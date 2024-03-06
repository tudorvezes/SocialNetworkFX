package cs.ubb.socialnetworkfx.service;

import cs.ubb.socialnetworkfx.domain.*;
import cs.ubb.socialnetworkfx.dto.*;
import cs.ubb.socialnetworkfx.repository.Repository;
import cs.ubb.socialnetworkfx.repository.RepositoryException;
import cs.ubb.socialnetworkfx.repository.paging.PagingRepository;
import cs.ubb.socialnetworkfx.repository.passwordRepository.PasswordRepository;
import cs.ubb.socialnetworkfx.utils.Constants;
import cs.ubb.socialnetworkfx.utils.events.ChangeEventType;
import cs.ubb.socialnetworkfx.utils.events.UserChangeEvent;
import cs.ubb.socialnetworkfx.utils.idGetter.IdGetter;
import cs.ubb.socialnetworkfx.utils.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class GeneralService {
    private final PagingRepository<Long, User, UserFilterDTO> userDatabase;
    private final Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase;
    private final Repository<Long, FriendshipRequest, FilterDTO> friendshipRequestDatabase;
    private final Repository<Long, ChatRoom, ChatRoomFilterDTO> chatRoomDatabase;
    private final Repository<Long, Message, MessageFilterDTO> messageDatabase;
    private final PagingRepository<Long, Post, PostFilterDTO> postDatabase;
    private final PasswordRepository<Long> passwordRepository;
    private final IdGetter<Long> idGetter;


    public GeneralService(PagingRepository<Long, User, UserFilterDTO> userDatabase,
                          Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase,
                          Repository<Long, FriendshipRequest, FilterDTO> friendshipRequestDatabase,
                          Repository<Long, ChatRoom, ChatRoomFilterDTO> chatRoomDatabase,
                          Repository<Long, Message, MessageFilterDTO> messageDatabase,
                          PagingRepository<Long, Post, PostFilterDTO> postDatabase,
                          PasswordRepository<Long> passwordRepository,
                          IdGetter<Long> idGetter) {
        this.userDatabase = userDatabase;
        this.friendshipDatabase = friendshipDatabase;
        this.friendshipRequestDatabase = friendshipRequestDatabase;
        this.chatRoomDatabase = chatRoomDatabase;
        this.messageDatabase = messageDatabase;
        this.postDatabase = postDatabase;
        this.passwordRepository = passwordRepository;
        this.idGetter = idGetter;
    }

    public AdminService getAdminService() {
        return AdminService.getInstance(userDatabase, friendshipDatabase, idGetter);
    }

    public UserService getUserService() {
        return UserService.getInstance(userDatabase, friendshipDatabase, friendshipRequestDatabase, chatRoomDatabase, messageDatabase, postDatabase, idGetter);
    }

    private Optional<User> findByUsername(String username) {
        UserFilterDTO userFilterDTO = new UserFilterDTO();
        userFilterDTO.setUsername(username);

        Iterable<User> users = userDatabase.findAllFiltered(userFilterDTO);
        return StreamSupport.stream(users.spliterator(), false)
                .filter(user -> user.getUsername().equals(username))
                .findFirst();

    }

    public Optional<User> logIn(String username, String password) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isEmpty()) {
            return Optional.empty();
        }
        User user = optionalUser.get();
        try {
            if(passwordRepository.verify(user.getId(), password)) {
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        } catch (RepositoryException e) {
            return Optional.empty();
        }

    }

    /**
     * Checks if the user has a password set
     * @param username the username of the user
     * @return an optional containing the user if he doesn't have a password set, empty otherwise
     */
    public Optional<User> hasPassword(String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isEmpty()) {
            return Optional.empty();
        }
        User user = optionalUser.get();
        if(!passwordRepository.exists(user.getId())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean validUsername(String username) {
        if(username.contains(" ")) {
            return false;
        }
        if(!username.matches("[a-z0-9_-]+")) {
            return false;
        }
        Optional<User> users = findByUsername(username);
        return users.isEmpty();
    }

    public int testPassword(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = !password.matches("[A-Za-z0-9 ]*");

        if (password.length() < 8) {
            return Constants.WEAK_PASSWORD;
        } else if (!hasUppercase || !hasLowercase || !hasDigit) {
            return Constants.MEDIUM_PASSWORD;
        } else if (hasSpecialChar) {
            return Constants.STRONG_PASSWORD;
        } else {
            return Constants.MEDIUM_PASSWORD;
        }
    }

    public void addUser(String username, String name, String password) {
        AdminService adminService = getAdminService();
        Long id = adminService.addUser(username, name);
        passwordRepository.save(id, password);
    }

    public void addPassword(User user, String password) {
        passwordRepository.save(user.getId(), password);
    }
}
