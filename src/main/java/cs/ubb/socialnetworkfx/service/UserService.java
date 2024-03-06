package cs.ubb.socialnetworkfx.service;

import cs.ubb.socialnetworkfx.domain.*;
import cs.ubb.socialnetworkfx.dto.*;
import cs.ubb.socialnetworkfx.repository.Repository;
import cs.ubb.socialnetworkfx.repository.paging.Page;
import cs.ubb.socialnetworkfx.repository.paging.Pageable;
import cs.ubb.socialnetworkfx.repository.paging.PagingRepository;
import cs.ubb.socialnetworkfx.utils.Constants;
import cs.ubb.socialnetworkfx.utils.events.ChangeEventType;
import cs.ubb.socialnetworkfx.utils.events.ChatRoomChangeEvent;
import cs.ubb.socialnetworkfx.utils.events.StatusChangeEvent;
import cs.ubb.socialnetworkfx.utils.idGetter.IdGetter;
import cs.ubb.socialnetworkfx.utils.observer.*;
import cs.ubb.socialnetworkfx.utils.observer.Observable;
import cs.ubb.socialnetworkfx.utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class UserService implements StatusObservable, ChatRoomObservable {
    private static UserService instance;

    private final Repository<Long, User, UserFilterDTO> userDatabase;
    private final Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase;
    private final Repository<Long, FriendshipRequest, FilterDTO> friendshipRequestDatabase;
    private final Repository<Long, ChatRoom, ChatRoomFilterDTO> chatRoomDatabase;
    private final Repository<Long, Message, MessageFilterDTO> messageDatabase;
    private final PagingRepository<Long, Post, PostFilterDTO> postDatabase;
    private final IdGetter<Long> idGetter;
    private final List<StatusObserver> profileObservers = new ArrayList<>();
    private final List<ChatRoomObserver> messangerObservers = new ArrayList<>();

    private UserService(Repository<Long, User, UserFilterDTO> userDatabase,
                        Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase,
                        Repository<Long, FriendshipRequest, FilterDTO> friendshipRequestDatabase,
                        Repository<Long, ChatRoom, ChatRoomFilterDTO> chatRoomDatabase,
                        Repository<Long, Message, MessageFilterDTO> messageDatabase,
                        PagingRepository<Long, Post, PostFilterDTO> postDatabase,
                        IdGetter<Long> idGetter) {
        this.userDatabase = userDatabase;
        this.friendshipDatabase = friendshipDatabase;
        this.friendshipRequestDatabase = friendshipRequestDatabase;
        this.chatRoomDatabase = chatRoomDatabase;
        this.messageDatabase = messageDatabase;
        this.postDatabase = postDatabase;
        this.idGetter = idGetter;
    }

    public static synchronized UserService getInstance(Repository<Long, User, UserFilterDTO> userDatabase,
                                                       Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase,
                                                       Repository<Long, FriendshipRequest, FilterDTO> friendshipRequestDatabase,
                                                       Repository<Long, ChatRoom, ChatRoomFilterDTO> chatRoomDatabase,
                                                       Repository<Long, Message, MessageFilterDTO> messageDatabase,
                                                       PagingRepository<Long, Post, PostFilterDTO> postDatabase,
                                                       IdGetter<Long> idGetter) {
        if (instance == null) {
            instance = new UserService(userDatabase, friendshipDatabase, friendshipRequestDatabase, chatRoomDatabase, messageDatabase, postDatabase, idGetter);
        }
        return instance;
    }

    public Optional<User> getUserById(Long id) {
        return userDatabase.findOne(id);
    }

    public Optional<User> getUserByUsername(String username) {
        UserFilterDTO userFilterDTO = new UserFilterDTO();
        userFilterDTO.setUsername(username);

        Iterable<User> users = userDatabase.findAllFiltered(userFilterDTO);
        return StreamSupport.stream(users.spliterator(), false)
                .filter(user -> user.getUsername().equals(username))
                .findFirst();

    }

    public List<User> getFriends(User user) {
        Predicate<Friendship> condition = friendship -> friendship.getUser1().equals(user.getId()) || friendship.getUser2().equals(user.getId());
        return StreamSupport.stream(friendshipDatabase.findAll().spliterator(), false)
                .filter(condition)
                .map(friendship -> {
                    if(friendship.getUser1().equals(user.getId())) {
                        return userDatabase.findOne(friendship.getUser2()).get();
                    } else {
                        return userDatabase.findOne(friendship.getUser1()).get();
                    }
                })
                .toList();
    }

    public Iterable<User> getAll() {
        return userDatabase.findAll();
    }

    public Iterable<Friendship> getAllFriendships() {
        return friendshipDatabase.findAll();
    }

    public Iterable<FriendshipRequest> getAllFriendshipRequests() {
        return friendshipRequestDatabase.findAll();
    }

    public void deleteAccount(User user) {
        userDatabase.delete(user.getId());
    }

    public Iterable<FriendshipRequest> getAllFriendshipRequests(User user) {
        Predicate<FriendshipRequest> condition = friendshipRequest -> friendshipRequest.getTo().equals(user.getId());
        return StreamSupport.stream(friendshipRequestDatabase.findAll().spliterator(), false)
                .filter(condition)
                .toList();
    }

    public void sendFriendshipRequest(User currentUser, User user) {
        FriendshipRequest friendshipRequest = new FriendshipRequest(currentUser.getId(), user.getId(), "pending");
        friendshipRequest.setId(idGetter.getUniqueId());
        friendshipRequestDatabase.save(friendshipRequest);
        StatusChangeEvent statusChangeEvent = new StatusChangeEvent(ChangeEventType.REQUEST_FRIEND, currentUser, user);
        notifyObservers(statusChangeEvent);
    }

    public void acceptFriendshipRequest(User currentUser, User user) {
        Predicate<FriendshipRequest> condition = friendshipRequest ->
                friendshipRequest.getFrom().equals(user.getId()) && friendshipRequest.getTo().equals(currentUser.getId());

        Optional<FriendshipRequest> friendshipRequest = StreamSupport.stream(friendshipRequestDatabase.findAll().spliterator(), false)
                .filter(condition)
                .findFirst();

        if (friendshipRequest.isPresent()) {
            Friendship friendship = new Friendship(friendshipRequest.get().getFrom(), friendshipRequest.get().getTo(), LocalDateTime.now());
            friendship.setId(idGetter.getUniqueId());
            friendshipDatabase.save(friendship);
            friendshipRequestDatabase.delete(friendshipRequest.get().getId());
            StatusChangeEvent statusChangeEvent = new StatusChangeEvent(ChangeEventType.ACCEPT_FRIEND, currentUser, user);
            notifyObservers(statusChangeEvent);
        }
    }

    public void declineFriendshipRequest(User currentUser, User user) {
        Predicate<FriendshipRequest> condition = friendshipRequest ->
                friendshipRequest.getFrom().equals(user.getId()) && friendshipRequest.getTo().equals(currentUser.getId());

        Optional<FriendshipRequest> friendshipRequest = StreamSupport.stream(friendshipRequestDatabase.findAll().spliterator(), false)
                .filter(condition)
                .findFirst();

        friendshipRequest.ifPresent(value -> friendshipRequestDatabase.delete(value.getId()));
        StatusChangeEvent statusChangeEvent = new StatusChangeEvent(ChangeEventType.DECLINE_FRIEND, currentUser, user);
        notifyObservers(statusChangeEvent);
    }

    public void removeFriend(User currentUser, User user) {
        Predicate<Friendship> condition = friendship -> friendship.getUser1().equals(currentUser.getId()) && friendship.getUser2().equals(user.getId()) || friendship.getUser1().equals(user.getId()) && friendship.getUser2().equals(currentUser.getId());
        StreamSupport.stream(friendshipDatabase.findAll().spliterator(), false)
                .filter(condition)
                .findFirst()
                .ifPresent(friendship -> friendshipDatabase.delete(friendship.getId()));
        StatusChangeEvent statusChangeEvent = new StatusChangeEvent(ChangeEventType.REMOVE_FRIEND, currentUser, user);
        notifyObservers(statusChangeEvent);
    }

    public boolean isFriend(User currentUser, User user) {
        FriendshipFilterDTO friendshipFilterDTO = new FriendshipFilterDTO();
        friendshipFilterDTO.setUser1(currentUser.getId());
        friendshipFilterDTO.setUser2(user.getId());
        return StreamSupport.stream(friendshipDatabase.findAllFiltered(friendshipFilterDTO).spliterator(), false)
                .findFirst()
                .isPresent();
    }

    public int getStatus(User currentUser, User user) {
        Predicate<FriendshipRequest> condition = friendshipRequest ->
                (friendshipRequest.getFrom().equals(currentUser.getId()) && friendshipRequest.getTo().equals(user.getId())) ||
                        (friendshipRequest.getFrom().equals(user.getId()) && friendshipRequest.getTo().equals(currentUser.getId()));

        Optional<FriendshipRequest> friendshipRequest = StreamSupport.stream(friendshipRequestDatabase.findAll().spliterator(), false)
                .filter(condition)
                .findFirst();

        if (friendshipRequest.isPresent()) {
            if (friendshipRequest.get().getFrom().equals(currentUser.getId())) {
                return Constants.PENDING_REQUEST;
            } else {
                return Constants.RECEIVED_REQUEST;
            }
        } else if (isFriend(currentUser, user)) {
            return Constants.ESTABLISHED_FRIENDSHIP;
        } else {
            return Constants.NO_FRIENDSHIP;
        }
    }

    public List<ChatRoom> getChatRooms(User currentUser) {
        ChatRoomFilterDTO chatRoomFilterDTO = new ChatRoomFilterDTO();
        chatRoomFilterDTO.setUserIds(List.of(currentUser.getId()));
        return StreamSupport.stream(chatRoomDatabase.findAllFiltered(chatRoomFilterDTO).spliterator(), false)
                .toList();
    }

    public void createChatRoom(String name, User creator, List<User> users) {
        users.add(creator);
        ChatRoom chatRoom = new ChatRoom(name, users);
        chatRoom.setId(idGetter.getUniqueId());
        if(users.size() == 2) {
            chatRoom.setType(Constants.PRIVATE_CHAT);
            chatRoom.setName(users.get(0).getUsername() + " & " + users.get(1).getUsername());
        } else {
            chatRoom.setType(Constants.GROUP_CHAT);
        }
        Optional<ChatRoom> optionalChatRoom = chatRoomDatabase.save(chatRoom);
        if(optionalChatRoom.isPresent()) {
            throw new ServiceException("Chat room already exists!");
        }

        ChatRoomChangeEvent chatRoomChangeEvent = new ChatRoomChangeEvent(ChangeEventType.CREATED_CHATROOM, creator, users, chatRoom);
        notifyObservers(chatRoomChangeEvent);
    }

    public ChatRoom populateChatRoom(ChatRoom chatRoom) {
        MessageFilterDTO messageFilterDTO = new MessageFilterDTO();
        messageFilterDTO.setChatRoomId(chatRoom.getId());
        Iterable<Message> messages = messageDatabase.findAllFiltered(messageFilterDTO);
        chatRoom.setMessages(messages);
        return chatRoom;
    }

    public void sendMessage(ChatRoom chatRoom, User sender, String content) {
        Message message = new Message(sender, chatRoom.getId(), content, LocalDateTime.now());
        message.setId(idGetter.getUniqueId());

        messageDatabase.save(message);

        chatRoom.setLastMessageDate(message.getDate());
        Optional<ChatRoom> optionalChatRoom = chatRoomDatabase.update(chatRoom);
        if(optionalChatRoom.isPresent()) {
            throw new ServiceException("Chat room does not exist!");
        }

        ChatRoomChangeEvent chatRoomChangeEvent = new ChatRoomChangeEvent(ChangeEventType.SENT_MESSAGE, sender, chatRoom.getUsers(), chatRoom, message);
        notifyObservers(chatRoomChangeEvent);
    }

    public List<Message> getMessages(ChatRoom chatRoom) {
        MessageFilterDTO messageFilterDTO = new MessageFilterDTO();
        messageFilterDTO.setChatRoomId(chatRoom.getId());
        return StreamSupport.stream(messageDatabase.findAllFiltered(messageFilterDTO).spliterator(), false)
                .toList();
    }

    public void addPost(User currentUser, String message) {
        Post post = new Post(currentUser.getId(), message, LocalDateTime.now());
        post.setId(idGetter.getUniqueId());
        postDatabase.save(post);
    }

    public List<Post> getPosts(User user) {
        PostFilterDTO postFilterDTO = new PostFilterDTO();
        postFilterDTO.setUserIds(List.of(user.getId()));
        return StreamSupport.stream(postDatabase.findAllFiltered(postFilterDTO).spliterator(), false)
                .sorted(Comparator.comparing(Post::getDate).reversed())
                .toList();
    }

    public Page<Post> getPosts(User user, Pageable pageable) {
        PostFilterDTO postFilterDTO = new PostFilterDTO();
        postFilterDTO.setUserIds(List.of(user.getId()));

        return postDatabase.findAllOnPageFiltered(pageable, postFilterDTO);
    }

    public List<Post> getFeed(User currentUser) {
        List<User> friends = getFriends(currentUser);

        PostFilterDTO postFilterDTO = new PostFilterDTO();
        postFilterDTO.setUserIds(StreamSupport.stream(friends.spliterator(), false)
                .map(User::getId)
                .toList());
        return StreamSupport.stream(postDatabase.findAllFiltered(postFilterDTO).spliterator(), false)
                .sorted(Comparator.comparing(Post::getDate).reversed())
                .toList();
    }

    public Page<Post> getFeed(User currentUser, Pageable pageable) {
        List<User> friends = getFriends(currentUser);
        friends.add(currentUser);
        PostFilterDTO postFilterDTO = new PostFilterDTO();
        postFilterDTO.setUserIds(StreamSupport.stream(friends.spliterator(), false)
                .map(User::getId)
                .toList());
        return postDatabase.findAllOnPageFiltered(pageable, postFilterDTO);
    }

    public List<User> searchByUsername(User currentUser, String username, int limit) {
        UserFilterDTO userFilterDTO = new UserFilterDTO();
        userFilterDTO.setUsername(username);
        userFilterDTO.setMaxSearch(limit);
        return StreamSupport.stream(userDatabase.findAllFiltered(userFilterDTO).spliterator(), false)
                .filter(user -> !user.equals(currentUser))
                .toList();
    }

    @Override
    public void addObserver(StatusObserver e) {
        profileObservers.add(e);
    }

    @Override
    public void removeObserver(StatusObserver e) {
        profileObservers.remove(e);
    }

    @Override
    public void notifyObservers(StatusChangeEvent t) {
        profileObservers.forEach(x -> x.update(t));
    }

    @Override
    public void addObserver(ChatRoomObserver e) {
        messangerObservers.add(e);
    }

    @Override
    public void removeObserver(ChatRoomObserver e) {
        messangerObservers.remove(e);
    }

    @Override
    public void notifyObservers(ChatRoomChangeEvent t) {
        messangerObservers.forEach(x -> x.update(t));
    }
}
