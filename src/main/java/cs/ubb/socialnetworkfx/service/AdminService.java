package cs.ubb.socialnetworkfx.service;

import cs.ubb.socialnetworkfx.domain.*;
import cs.ubb.socialnetworkfx.domain.validator.ValidationException;
import cs.ubb.socialnetworkfx.dto.*;
import cs.ubb.socialnetworkfx.repository.ChatRoomDBRepository;
import cs.ubb.socialnetworkfx.repository.Repository;
import cs.ubb.socialnetworkfx.repository.RepositoryException;
import cs.ubb.socialnetworkfx.repository.paging.Page;
import cs.ubb.socialnetworkfx.repository.paging.Pageable;
import cs.ubb.socialnetworkfx.repository.paging.PagingRepository;
import cs.ubb.socialnetworkfx.repository.passwordRepository.PasswordRepository;
import cs.ubb.socialnetworkfx.utils.Constants;
import cs.ubb.socialnetworkfx.utils.events.ChangeEventType;
import cs.ubb.socialnetworkfx.utils.events.UserChangeEvent;
import cs.ubb.socialnetworkfx.utils.idGetter.IdGetter;
import cs.ubb.socialnetworkfx.utils.observer.Observable;
import cs.ubb.socialnetworkfx.utils.observer.Observer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AdminService implements Observable<UserChangeEvent> {
    private static AdminService instance;
    private final PagingRepository<Long, User, UserFilterDTO> userDatabase;
    private final Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase;
    private final List<Observer<UserChangeEvent>> observers = new ArrayList<>();
    private final IdGetter<Long> idGetter;

    /**
     * Finds a user in the user database by username
     * @param username The username of the user to be found
     * @return The user if it exists, null otherwise
     */
    private Optional<User> findByUsername(String username) {
        UserFilterDTO userFilterDTO = new UserFilterDTO();
        userFilterDTO.setUsername(username);

        Iterable<User> users = userDatabase.findAllFiltered(userFilterDTO);
        return StreamSupport.stream(users.spliterator(), false)
                .filter(user -> user.getUsername().equals(username))
                .findFirst();

    }

    /**
     * Finds a friendship in the friendship database
     * @param friendship The friendship to be found
     * @return The friendship if it exists, null otherwise
     */
    private Optional<Friendship> findFriendship(Friendship friendship) {
        FriendshipFilterDTO friendshipFilterDTO = new FriendshipFilterDTO();
        friendshipFilterDTO.setUser1(friendship.getUser1());
        friendshipFilterDTO.setUser2(friendship.getUser2());

        Iterable<Friendship> friendships = friendshipDatabase.findAllFiltered(friendshipFilterDTO);
        return StreamSupport.stream(friendships.spliterator(), false)
                .filter(friendship1 -> friendship1.equals(friendship))
                .findFirst();
    }

    /**
     * Constructor for AdminService
     * @param users The user database
     * @param friendships The friendship database
     */
    private AdminService(PagingRepository<Long, User, UserFilterDTO> users,
                        Repository<Long, Friendship, FriendshipFilterDTO> friendships,
                        IdGetter<Long> idGetter) {
        this.userDatabase = users;
        this.friendshipDatabase = friendships;

        this.idGetter = idGetter;
    }

    public static AdminService getInstance(PagingRepository<Long, User, UserFilterDTO> users,
                                    Repository<Long, Friendship, FriendshipFilterDTO> friendships,
                                    IdGetter<Long> idGetter) {
        if(instance == null) {
            instance = new AdminService(users, friendships, idGetter);
        }
        return instance;
    }

    public Optional<User> addUser(Long id, String username, String name) {
        User user = new User(idGetter.getUniqueId(id), username, name);
        Optional<User> optionalUser= userDatabase.save(user);
        if(optionalUser.isPresent()) {
            throw new ServiceException("User already exists!");
        } else {
            notifyObservers(new UserChangeEvent(ChangeEventType.ADD, user));
        }
        return Optional.of(user);
    }

    /**
     * Adds a user to the user database
     * @param username The username of the user
     * @param name The name of the user
     */
    public Long addUser(String username, String name) {
        User user = new User(idGetter.getUniqueId(), username, name);
        Optional<User> optionalUser= userDatabase.save(user);
        if(optionalUser.isPresent()) {
            throw new ServiceException("User already exists!");
        } else {
            notifyObservers(new UserChangeEvent(ChangeEventType.ADD, user));
            return user.getId();
        }
    }

    /**
     * Adds users to the database from a csv file
     * @param filepath The path to the file containing the users
     */
    public void importUsersCSV(String filepath) {
        StringBuilder errors = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                String[] data = line.split(",");

                String id_string = data[0];
                Long id = Long.parseLong(id_string);
                String username = data[1];
                String name = data[2];

                try {
                    User user = new User(idGetter.getUniqueId(id), username, name);
                    Optional<User> optionalUser = userDatabase.save(user);
                    if(optionalUser.isPresent()) {
                        throw new ServiceException("User already exists!");
                    } else {
                        notifyObservers(new UserChangeEvent(ChangeEventType.ADD, user));
                    }
                } catch (ServiceException | RepositoryException | ValidationException | IllegalArgumentException e) {
                    errors.append("Line ").append(ln).append(": ").append(e).append("\n");
                }

            }
        } catch (IOException e) {
            throw new FileReadingException("Error reading file: " + e);
        }
        if(!errors.isEmpty()) {
            errors.insert(0, "\n");
            throw new FileReadingException(errors.toString());
        }
    }

    /**
     * Removes a user from the database
     * @param id the id of the user
     */
    public Optional<User> removeUserByID(Long id) {
        Optional<User> optionalUser = userDatabase.delete(id);
        if(optionalUser.isEmpty()) {
            throw new ServiceException("User does not exist!");
        } else {
            notifyObservers(new UserChangeEvent(ChangeEventType.DELETE, optionalUser.get()));
        }
        return optionalUser;
    }

    /**
     * Removes a user from the database
     * @param username the username of the user
     */
    public void removeUserByUsername(String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isEmpty()) {
            throw new ServiceException("User does not exist!");
        } else {
            notifyObservers(new UserChangeEvent(ChangeEventType.DELETE, optionalUser.get()));
        }
        userDatabase.delete(optionalUser.get().getId());
    }

    /**
     * Updates a user in the database
     * @param id The id of the user
     * @param username The new username of the user
     * @param name The new name of the user
     * @throws ServiceException if the user does not exist
     */
    public void updateUser(Long id, String username, String name) {
        User newUser = new User(id, username, name);
        Optional<User> optionalOldUser = userDatabase.findOne(id);
        Optional<User> optionalUser = userDatabase.update(newUser);
        if(optionalUser.isPresent()) {
            throw new ServiceException("User does not exist!");
        } else {
            optionalOldUser.ifPresent(user -> notifyObservers(new UserChangeEvent(ChangeEventType.UPDATE, newUser, user)));
        }
    }

    /**
     * Adds a friendship to the database
     * @param id1 The id of the first user
     * @param id2 The id of the second user
     */
    public void addFriendship(Long id1, Long id2) {
        Optional<User> optionalUser1 = userDatabase.findOne(id1);
        Optional<User> optionalUser2 = userDatabase.findOne(id2);

        if(optionalUser1.isEmpty()) {
            throw new ServiceException("User with id " + id1 + " does not exist!");
        }
        if(optionalUser2.isEmpty()) {
            throw new ServiceException("User with id " + id2 + " does not exist!");
        }

        Friendship friendship = new Friendship(id1, id2, LocalDateTime.now());
        friendship.setId(idGetter.getUniqueId());
        Optional<Friendship> optionalFriendship = friendshipDatabase.save(friendship);
        if(optionalFriendship.isPresent()) {
            throw new ServiceException("Friendship already exists!");
        }
    }

    public void addFriendship(Long friendshipID, Long id1, Long id2, LocalDateTime date) {
        Optional<User> optionalUser1 = userDatabase.findOne(id1);
        Optional<User> optionalUser2 = userDatabase.findOne(id2);

        if(optionalUser1.isEmpty()) {
            throw new ServiceException("User with id " + id1 + " does not exist!");
        }
        if(optionalUser2.isEmpty()) {
            throw new ServiceException("User with id " + id2 + " does not exist!");
        }

        Friendship friendship = new Friendship(id1, id2, date);
        friendship.setId(idGetter.getUniqueId(friendshipID));
        Optional<Friendship> optionalFriendship = friendshipDatabase.save(friendship);
        if(optionalFriendship.isPresent()) {
            throw new ServiceException("Friendship already exists!");
        }
    }

    /**
     * Adds a friendship to the database
     * @param username1 The username of the first user
     * @param username2 The username of the second user
     */
    public void addFriendship(String username1, String username2) {
        Optional<User> optionalUser1 = findByUsername(username1);
        Optional<User> optionalUser2 = findByUsername(username2);

        if(optionalUser1.isEmpty()) {
            if(optionalUser2.isEmpty()) {
                throw new ServiceException("Users with usernames " + username1 + " and " + username2 + " do not exist!");
            }
            throw new ServiceException("User with username " + username1 + " does not exist!");
        }
        if(optionalUser2.isEmpty()) {
            throw new ServiceException("User with username " + username2 + " does not exist!");
        }
        User user1 = optionalUser1.get();
        User user2 = optionalUser2.get();

        Friendship friendship = new Friendship(user1, user2, LocalDateTime.now());
        friendship.setId(idGetter.getUniqueId());
        Optional<Friendship> optionalFriendship = friendshipDatabase.save(friendship);
        if(optionalFriendship.isPresent()) {
            throw new ServiceException("Friendship already exists!");
        }
    }

    public void addFriendship(Long id, String username1, String username2) {
        Optional<User> optionalUser1 = findByUsername(username1);
        Optional<User> optionalUser2 = findByUsername(username2);

        if(optionalUser1.isEmpty()) {
            if(optionalUser2.isEmpty()) {
                throw new ServiceException("Users with usernames " + username1 + " and " + username2 + " do not exist!");
            }
            throw new ServiceException("User with username " + username1 + " does not exist!");
        }
        if(optionalUser2.isEmpty()) {
            throw new ServiceException("User with username " + username2 + " does not exist!");
        }
        User user1 = optionalUser1.get();
        User user2 = optionalUser2.get();

        Friendship friendship = new Friendship(user1, user2, LocalDateTime.now());
        friendship.setId(idGetter.getUniqueId(id));

        Optional<Friendship> optionalFriendship = friendshipDatabase.save(friendship);
        if(optionalFriendship.isPresent()) {
            throw new ServiceException("Friendship already exists!");
        }
    }

    /**
     * Adds friendships to the database from a csv file
     * @param filepath The path to the file containing the friendships
     */
    public void importFriendshipsCSV(String filepath) {
        StringBuilder errors = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                String[] data = line.split(",");

                String id_string = data[0];
                Long friendshipId = Long.parseLong(id_string);
                String user1Id_string = data[1];
                Long user1Id = Long.parseLong(user1Id_string);
                String user2Id_string = data[2];
                Long user2Id = Long.parseLong(user2Id_string);
                String dateString = data[3].replace("\"", ""); // Remove double quotes

                try {
                    LocalDateTime date = LocalDateTime.parse(dateString, formatter);
                    addFriendship(friendshipId, user1Id, user2Id, date);
                } catch (ServiceException | RepositoryException | ValidationException e) {
                    errors.append("Line ").append(ln).append(": ").append(e).append("\n");
                }

            }
        } catch (IOException e) {
            throw new ServiceException("Error reading file: " + e);
        }
        if (!errors.isEmpty()) {
            errors.insert(0, "\n");
            throw new ServiceException(errors.toString());
        }
    }

    /**
     * Removes a friendship from the database
     * @param id1 The id of the first user
     * @param id2 The id of the second user
     */
    public void removeFriendship(Long id1, Long id2) {
        Optional<User> optionalUser1 = userDatabase.findOne(id1);
        Optional<User> optionalUser2 = userDatabase.findOne(id2);

        if(optionalUser1.isEmpty()) {
            if(optionalUser2.isEmpty()) {
                throw new ServiceException("Users with ids " + id1 + " and " + id2 + " do not exist!");
            }
            throw new ServiceException("User with id " + id1 + " does not exist!");
        }
        if(optionalUser2.isEmpty()) {
            throw new ServiceException("User with id " + id2 + " does not exist!");
        }

        Optional<Friendship> friendship = findFriendship(new Friendship(id1, id2, LocalDateTime.now()));
        if(friendship.isEmpty()) {
            throw new ServiceException("Friendship does not exist!");
        }
        friendshipDatabase.delete(friendship.get().getId());
    }

    /**
     * Removes a friendship from the database
     * @param username1 The username of the first user
     * @param username2 The username of the second user
     */
    public void removeFriendship(String username1, String username2) {
        Optional<User> optionalUser1 = findByUsername(username1);
        Optional<User> optionalUser2 = findByUsername(username2);

        if(optionalUser1.isEmpty()) {
            if(optionalUser2.isEmpty()) {
                throw new ServiceException("Users with usernames " + username1 + " and " + username2 + " do not exist!");
            }
            throw new ServiceException("User with username " + username1 + " does not exist!");
        }
        if(optionalUser2.isEmpty()) {
            throw new ServiceException("User with username " + username2 + " does not exist!");
        }
        User user1 = optionalUser1.get();
        User user2 = optionalUser2.get();

        Optional<Friendship> friendship = findFriendship(new Friendship(user1, user2, LocalDateTime.now()));
        if(friendship.isEmpty()) {
            throw new ServiceException("Friendship does not exist!");
        }
        friendshipDatabase.delete(friendship.get().getId());
    }

    /**
     * Gets all the friendships of a user
     * @param username The username of the user
     * @param day The day from which to get the friendships
     * @param month The month from which to get the friendships
     * @param year The year from which to get the friendships
     * @return A list containing the friendships of the user
     */
    public List<Friendship> getFriendshipsSince(String username, int day, int month, int year) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isEmpty()) {
            throw new ServiceException("User with username " + username + " does not exist!");
        }
        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);

        List<Friendship> friendshipsList = StreamSupport.stream(friendshipDatabase.findAll().spliterator(), false)
                .filter(friendship -> friendship.getDate().isAfter(date))
                .filter(friendship -> friendship.getUser1().equals(optionalUser.get().getId()) || friendship.getUser2().equals(optionalUser.get().getId()))
                .sorted(Comparator.comparing(Friendship::getDate))
                .collect(Collectors.toList());

        return friendshipsList;
    }

    /**
     * Gets the other user in a friendship
     * @param friendship The friendship
     * @param username The username of the user
     * @return The other user in the friendship
     */
    public User getOtherUser(Friendship friendship, String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isEmpty()) {
            throw new ServiceException("User with username " + username + " does not exist!");
        }
        User user = optionalUser.get();
        if(friendship.getUser1().equals(user.getId())) {
            return getUserById(friendship.getUser2());
        } else if(friendship.getUser2().equals(user.getId())) {
            return getUserById(friendship.getUser1());
        } else {
            throw new ServiceException("User with username " + username + " is not part of the friendship!");
        }
    }

    public Long getNextId() {
        return idGetter.getCurrentId() + 1;
    }

    /**
     * Gets all the users from the database
     * @return An iterable containing all the users
     */
    public Iterable<User> getAllUsers() {
        return userDatabase.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userDatabase.findAllOnPage(pageable);
    }

    /**
     * Gets all the friendships from the database
     * @return An iterable containing all the friendships
     */
    public Iterable<Friendship> getAllFriendships() {
        return friendshipDatabase.findAll();
    }

    public User getUserById(Long id) {
        return userDatabase.findOne(id).orElse(null);
    }

    /**
     * Gets the number of users in the database
     * @return The number of users
     */
    public int getNumberOfCommunities() {
        Graph graph = new Graph(userDatabase, friendshipDatabase);
        return graph.countConnectedComponents();
    }

    /**
     * Gets the longest road component
     * @return A list containing the users in the longest road component
     */
    public List<User> getLongestRoadComponent() {
        Graph graph = new Graph(userDatabase, friendshipDatabase);
        return graph.findLongestRoadComponent();
    }

    @Override
    public void addObserver(Observer<UserChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<UserChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(UserChangeEvent t) {
        observers.forEach(x->x.update(t));
    }
}


