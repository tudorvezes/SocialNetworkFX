package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.ChatRoom;
import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.dto.ChatRoomFilterDTO;
import cs.ubb.socialnetworkfx.utils.Constants;
import cs.ubb.socialnetworkfx.utils.Pair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

public class ChatRoomDBRepository implements Repository<Long, ChatRoom, ChatRoomFilterDTO> {
    private final String url;
    private final String username;
    private final String password;

    public ChatRoomDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<ChatRoom> findOne(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement chatRoomsStatement = connection.prepareStatement("select * from ChatRooms " +
                     "where id = ?");
             PreparedStatement usersInChatRoomStatement = connection.prepareStatement("select U.id, U.username, U.name " +
                     "from Users as U " + "inner join usersInChatRoom as UCR on U.id=UCR.user_id " + "where UCR.chatroom_id=?");
        ) {
            chatRoomsStatement.setInt(1, Math.toIntExact(id));
            ResultSet chatRoomsResultSet = chatRoomsStatement.executeQuery();

            if(chatRoomsResultSet.next()) {
                String name = chatRoomsResultSet.getString("name");
                LocalDateTime lastMessageDate = chatRoomsResultSet.getTimestamp("last_message_timestamp").toLocalDateTime();
                List<User> users = new ArrayList<>();

                usersInChatRoomStatement.setInt(1, Math.toIntExact(id));
                ResultSet usersInChatRoomResultSet = usersInChatRoomStatement.executeQuery();
                while(usersInChatRoomResultSet.next()) {
                    Long userId = usersInChatRoomResultSet.getLong("id");
                    String username = usersInChatRoomResultSet.getString("username");
                    String userName = usersInChatRoomResultSet.getString("name");
                    User user = new User(userId, username, userName);
                    users.add(user);
                }

                ChatRoom chatRoom = new ChatRoom(name, users);
                chatRoom.setId(id);
                chatRoom.setLastMessageDate(lastMessageDate);
                if(users.size() == 2) {
                    chatRoom.setType(Constants.PRIVATE_CHAT);
                } else {
                    chatRoom.setType(Constants.GROUP_CHAT);
                }
                return Optional.of(chatRoom);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<ChatRoom> findAll() {
        Set<ChatRoom> chatRooms = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement chatRoomsStatement = connection.prepareStatement("select * from ChatRooms");
             PreparedStatement usersInChatRoomStatement = connection.prepareStatement("select U.id, U.username, U.name " +
                     "from Users as U " + "inner join usersInChatRoom as UCR on U.id=UCR.user_id " + "where UCR.chatroom_id=?");
        ) {
            ResultSet chatRoomsResultSet = chatRoomsStatement.executeQuery();

            while(chatRoomsResultSet.next()) {
                Long id = chatRoomsResultSet.getLong("id");
                String name = chatRoomsResultSet.getString("name");
                LocalDateTime lastMessageDate = chatRoomsResultSet.getTimestamp("last_message_timestamp").toLocalDateTime();
                List<User> users = new ArrayList<>();

                usersInChatRoomStatement.setInt(1, Math.toIntExact(id));
                ResultSet usersInChatRoomResultSet = usersInChatRoomStatement.executeQuery();
                while(usersInChatRoomResultSet.next()) {
                    Long userId = usersInChatRoomResultSet.getLong("id");
                    String username = usersInChatRoomResultSet.getString("username");
                    String userName = usersInChatRoomResultSet.getString("name");
                    User user = new User(userId, username, userName);
                    users.add(user);
                }

                ChatRoom chatRoom = new ChatRoom(name, users);
                chatRoom.setId(id);
                chatRoom.setLastMessageDate(lastMessageDate);
                if(users.size() == 2) {
                    chatRoom.setType(Constants.PRIVATE_CHAT);
                } else {
                    chatRoom.setType(Constants.GROUP_CHAT);
                }
                chatRooms.add(chatRoom);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return chatRooms;
    }

    private Optional<ChatRoom> exists(ChatRoom chatRoom) {
        ChatRoomFilterDTO filter = new ChatRoomFilterDTO();
        filter.setUserIds(chatRoom.getUsers().stream().map(User::getId).toList());
        Iterable<ChatRoom> chatRooms = findAllFiltered(filter);

        return StreamSupport.stream(chatRooms.spliterator(), false)
                .filter(c -> c.equals(chatRoom))
                .findFirst();
    }

    @Override
    public Optional<ChatRoom> save(ChatRoom entity) {
        Optional<ChatRoom> optionalChatRoom = exists(entity);
        if(optionalChatRoom.isPresent()) {
            return optionalChatRoom;
        }

        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into ChatRooms (id, name, last_message_timestamp) " +
                    "values (?, ?, ?)");
            PreparedStatement usersInChatRoomStatement = connection.prepareStatement("insert into usersInChatRoom (user_id, chatroom_id) " +
                    "values (?, ?)");
        ) {
            statement.setInt(1, Math.toIntExact(entity.getId()));
            statement.setString(2, entity.getName());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getLastMessageDate()));
            statement.executeUpdate();

            for(User user : entity.getUsers()) {
                usersInChatRoomStatement.setInt(1, Math.toIntExact(user.getId()));
                usersInChatRoomStatement.setInt(2, Math.toIntExact(entity.getId()));
                usersInChatRoomStatement.executeUpdate();
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ChatRoom> delete(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("delete from ChatRooms where id = ?");
        ) {
            Optional<ChatRoom> optionalChatRoom = findOne(id);
            if(optionalChatRoom.isEmpty()) {
                return Optional.empty();
            }
            statement.setInt(1, Math.toIntExact(id));
            statement.executeUpdate();
            return optionalChatRoom;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ChatRoom> update(ChatRoom entity) {
        Optional<ChatRoom> optionalChatRoom = exists(entity);
        if(optionalChatRoom.isEmpty()) {
            return Optional.of(entity);
        }
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("update ChatRooms set name = ?, last_message_timestamp = ? where id = ?");
            PreparedStatement usersInChatRoomStatement = connection.prepareStatement("delete from usersInChatRoom where chatroom_id = ?");
            PreparedStatement usersInChatRoomStatement2 = connection.prepareStatement("insert into usersInChatRoom (user_id, chatroom_id) " +
                    "values (?, ?)");
        ) {
            statement.setString(1, entity.getName());
            statement.setTimestamp(2, Timestamp.valueOf(entity.getLastMessageDate()));
            statement.setInt(3, Math.toIntExact(entity.getId()));
            statement.executeUpdate();

            usersInChatRoomStatement.setInt(1, Math.toIntExact(entity.getId()));
            usersInChatRoomStatement.executeUpdate();

            for(User user : entity.getUsers()) {
                usersInChatRoomStatement2.setInt(1, Math.toIntExact(user.getId()));
                usersInChatRoomStatement2.setInt(2, Math.toIntExact(entity.getId()));
                usersInChatRoomStatement2.executeUpdate();
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Long> getAllKeys() {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select id from ChatRooms");
        ) {
            Set<Long> ids = new HashSet<>();
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                ids.add(resultSet.getLong("id"));
            }
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, List<Long>> toSQL(ChatRoomFilterDTO filter) {
        StringBuilder sql = new StringBuilder();
        List<Long> values = new ArrayList<>();

        if (filter.getUserIds().isPresent() && !filter.getUserIds().get().isEmpty()) {
            sql.append(" INNER JOIN UsersInChatRoom AS UCR ON CR.id = UCR.chatroom_id AND UCR.user_id IN (");
            for (int i = 0; i < filter.getUserIds().get().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append("?");
                values.add(filter.getUserIds().get().get(i));
            }
            sql.append(") ");
        }

        return new Pair<>(sql.toString(), values);
    }



    @Override
    public Iterable<ChatRoom> findAllFiltered(ChatRoomFilterDTO chatRoomFilterDTO) {
        Set<ChatRoom> chatRooms = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select CR.id, CR.name, CR.last_message_timestamp " +
                    "from ChatRooms AS CR " + toSQL(chatRoomFilterDTO).getFirst() + " GROUP BY CR.id" + " ORDER BY CR.last_message_timestamp DESC");
        ) {
            List<Long> values = toSQL(chatRoomFilterDTO).getSecond();
            for(int i = 0; i < values.size(); i++) {
                statement.setInt(i + 1, Math.toIntExact(values.get(i)));
            }
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                LocalDateTime lastMessageDate = resultSet.getTimestamp("last_message_timestamp").toLocalDateTime();
                List<User> users = new ArrayList<>();

                PreparedStatement usersInChatRoomStatement = connection.prepareStatement("select U.id, U.username, U.name " +
                        "from Users as U " + "inner join usersInChatRoom as UCR on U.id=UCR.user_id " + "where UCR.chatroom_id=?");
                usersInChatRoomStatement.setInt(1, Math.toIntExact(id));
                ResultSet usersInChatRoomResultSet = usersInChatRoomStatement.executeQuery();
                while(usersInChatRoomResultSet.next()) {
                    Long userId = usersInChatRoomResultSet.getLong("id");
                    String username = usersInChatRoomResultSet.getString("username");
                    String userName = usersInChatRoomResultSet.getString("name");
                    User user = new User(userId, username, userName);
                    users.add(user);
                }

                ChatRoom chatRoom = new ChatRoom(name, users);
                chatRoom.setId(id);
                chatRoom.setLastMessageDate(lastMessageDate);
                if(users.size() == 2) {
                    chatRoom.setType(Constants.PRIVATE_CHAT);
                } else {
                    chatRoom.setType(Constants.GROUP_CHAT);
                }
                chatRooms.add(chatRoom);
            }
            return chatRooms;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
