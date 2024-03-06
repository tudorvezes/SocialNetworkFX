package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.Message;
import cs.ubb.socialnetworkfx.domain.validator.Validator;
import cs.ubb.socialnetworkfx.dto.MessageFilterDTO;
import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.utils.Pair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDBRepository implements Repository<Long, Message, MessageFilterDTO> {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<Message> validator;

    public MessageDBRepository(String url, String username, String password, Validator<Message> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<Message> findOne(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT M.chatroom_id, M.sender, M.content, M.datestamp, " +
                             "U.username, U.name " +
                             "FROM Messages M " +
                             "INNER JOIN Users U ON M.sender = U.id " +
                             "WHERE M.id = ?");
        ) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Long chatRoomId = resultSet.getLong("chatroom_id");
                Long senderId = resultSet.getLong("sender");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");

                User u = new User(senderId, username, name);
                Message m = new Message(u, chatRoomId, content, datestamp);

                return Optional.of(m);
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }

        return Optional.empty();
    }


    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT M.id, M.chatroom_id, M.sender, M.content, M.datestamp, " +
                             "U.username, U.name " +
                             "FROM Messages M " +
                             "INNER JOIN Users U ON M.sender = U.id");
             ResultSet resultSet = statement.executeQuery();
        ) {
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long chatRoomId = resultSet.getLong("chatroom_id");
                Long senderId = resultSet.getLong("sender");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");

                User u = new User(senderId, username, name);
                Message m = new Message(u, chatRoomId, content, datestamp);
                m.setId(id);

                messages.add(m);
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }

        return messages;
    }


    @Override
    public Optional<Message> save(Message entity) {
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into Messages (id, chatroom_id, sender, content, datestamp) " +
                    "values (?, ?, ?, ?, ?)");
        ) {
            statement.setInt(1, Math.toIntExact(entity.getId()));
            statement.setInt(2, Math.toIntExact(entity.getChatRoomId()));
            statement.setInt(3, Math.toIntExact(entity.getSender().getId()));
            statement.setString(4, entity.getContent());
            statement.setTimestamp(5, Timestamp.valueOf(entity.getDate()));
            statement.executeUpdate();

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.of(entity);
    }

    @Override
    public Set<Long> getAllKeys() {
        Set<Long> keys = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select id from Messages");
            ResultSet resultSet = statement.executeQuery()
        ) {
            while(resultSet.next()) {
                keys.add(resultSet.getLong("id"));
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
        return keys;
    }

    private Pair<String, List<String>> toSQL(MessageFilterDTO filter) {
        StringBuilder sql = new StringBuilder();
        List<String> values = new ArrayList<>(List.of());
        sql.append(" where 1=1");
        if(filter.getChatRoomId().isPresent()) {
            sql.append(" and chatroom_id = CAST(? AS INTEGER) ");
            values.add(filter.getChatRoomId().get());
        }
        if(filter.getSenderId().isPresent()) {
            sql.append(" and sender = CAST(? AS INTEGER) ");
            values.add(filter.getSenderId().get());
        }
        if(filter.getContent().isPresent()) {
            sql.append(" and content like CONCAT('%', ?, '%')");
            values.add(filter.getContent().get());
        }
        return new Pair<>(sql.toString(), values);
    }

    @Override
    public Iterable<Message> findAllFiltered(MessageFilterDTO messageFilterDTO) {
        Set<Message> messages = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT M.id, M.chatroom_id, M.sender, M.content, M.datestamp, " +
                             "U.username, U.name " +
                             "FROM Messages M " +
                             "INNER JOIN Users U ON M.sender = U.id " +
                             toSQL(messageFilterDTO).getFirst() +
                             " ORDER BY M.datestamp ASC");
        ) {
            List<String> values = toSQL(messageFilterDTO).getSecond();
            for (int i = 0; i < values.size(); i++) {
                statement.setString(i + 1, values.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long chatRoomId = resultSet.getLong("chatroom_id");
                Long senderId = resultSet.getLong("sender");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");

                User u = new User(senderId, username, name);
                Message m = new Message(u, chatRoomId, content, datestamp);
                m.setId(id);

                messages.add(m);
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
        return messages;
    }

}
