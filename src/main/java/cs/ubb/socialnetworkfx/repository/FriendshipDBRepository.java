package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.Friendship;
import cs.ubb.socialnetworkfx.domain.validator.Validator;
import cs.ubb.socialnetworkfx.dto.FilterDTO;
import cs.ubb.socialnetworkfx.dto.FriendshipFilterDTO;
import cs.ubb.socialnetworkfx.utils.Pair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FriendshipDBRepository implements Repository<Long, Friendship, FriendshipFilterDTO>{
    private final String url;
    private final String username;
    private final String password;
    private final Validator<Friendship> validator;

    public FriendshipDBRepository(String url, String username, String password, Validator<Friendship> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<Friendship> findOne(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from Friendships " +
                    "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(id));
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                Long userId1 = resultSet.getLong("user1");
                Long userId2 = resultSet.getLong("user2");
                LocalDateTime datestamp = resultSet.getTimestamp("timedate").toLocalDateTime();
                Friendship f = new Friendship(userId1, userId2, datestamp);
                f.setId(id);
                return Optional.of(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Optional<Friendship> exists(Friendship friendship) {
        FriendshipFilterDTO filter = new FriendshipFilterDTO();
        filter.setUser1(friendship.getUser1());
        filter.setUser2(friendship.getUser2());

        Iterable<Friendship> friendships = findAllFiltered(filter);
        return StreamSupport.stream(friendships.spliterator(), false)
                .filter(f -> f.equals(friendship))
                .findFirst();
    }

    @Override
    public Iterable<Friendship> findAll() {
        Set<Friendship> friendships = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from Friendships");
            ResultSet resultSet = statement.executeQuery()
        ) {
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userId1 = resultSet.getLong("user1");
                Long userId2 = resultSet.getLong("user2");
                LocalDateTime datestamp = resultSet.getTimestamp("timedate").toLocalDateTime();
                Friendship f = new Friendship(userId1, userId2, datestamp);
                f.setId(id);
                friendships.add(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendships;
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        validator.validate(entity);
        Optional<Friendship> optionalFriendship = exists(entity);
        if(optionalFriendship.isPresent()) {
            return optionalFriendship;
        }
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into Friendships (id, user1, user2, timedate) values (?, ?, ?, ?)");
        ) {
            statement.setInt(1, Math.toIntExact(entity.getId()));
            statement.setInt(2, Math.toIntExact(entity.getUser1()));
            statement.setInt(3, Math.toIntExact(entity.getUser2()));
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            statement.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> delete(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("delete from Friendships where id = ?");
        ) {
            Optional<Friendship> optionalFriendship = findOne(id);
            if(optionalFriendship.isEmpty()) {
                return Optional.empty();
            }
            statement.setInt(1, Math.toIntExact(id));
            statement.executeUpdate();
            return optionalFriendship;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        Optional<Friendship> optionalFriendship = exists(entity);
        if(optionalFriendship.isEmpty()) {
            return Optional.of(entity);
        }
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("update Friendships set user1 = ?, user2 = ?, timedate = ? where id = ?");
        ) {
            statement.setInt(1, Math.toIntExact(entity.getUser1()));
            statement.setInt(2, Math.toIntExact(entity.getUser2()));
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            statement.setInt(4, Math.toIntExact(entity.getId()));
            statement.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Long> getAllKeys() {
        Set<Long> keys = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select id from Friendships");
            ResultSet resultSet = statement.executeQuery()
        ) {
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                keys.add(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }

    private Pair<String, List<Long>> toSQL(FriendshipFilterDTO filter) {
        StringBuilder sql = new StringBuilder();
        List<Long> values = new ArrayList<>();
        sql.append(" WHERE 1=1 ");

        if (filter.getUser1().isPresent()) {
            sql.append(" AND (user1 = ? OR user2 = ?) ");
            values.add(filter.getUser1().get());
            values.add(filter.getUser1().get());
        }

        if (filter.getUser2().isPresent()) {
            sql.append(" AND (user1 = ? OR user2 = ?) ");
            values.add(filter.getUser2().get());
            values.add(filter.getUser2().get());
        }

        return new Pair<>(sql.toString(), values);
    }

    @Override
    public Iterable<Friendship> findAllFiltered(FriendshipFilterDTO filter) {
        Set<Friendship> friendships = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM Friendships " + toSQL(filter).getFirst());
        ) {
            List<Long> values = toSQL(filter).getSecond();
            int parameterIndex = 1;
            for (Long value : values) {
                statement.setLong(parameterIndex++, value);
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userId1 = resultSet.getLong("user1");
                Long userId2 = resultSet.getLong("user2");
                LocalDateTime datestamp = resultSet.getTimestamp("timedate").toLocalDateTime();
                Friendship f = new Friendship(userId1, userId2, datestamp);
                f.setId(id);
                friendships.add(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendships;
    }


}
