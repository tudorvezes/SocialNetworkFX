package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.Friendship;
import cs.ubb.socialnetworkfx.domain.FriendshipRequest;
import cs.ubb.socialnetworkfx.dto.FilterDTO;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FriendshipRequestDBRepository implements Repository<Long, FriendshipRequest, FilterDTO> {
    private final String url;
    private final String username;
    private final String password;

    public FriendshipRequestDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<FriendshipRequest> findOne(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from FriendshipRequests " +
                    "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(id));
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                Long to = resultSet.getLong("to");
                Long from = resultSet.getLong("from");
                String status = resultSet.getString("status");
                FriendshipRequest fr = new FriendshipRequest(from, to, status);
                fr.setId(id);
                return Optional.of(fr);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Optional<FriendshipRequest> exists(FriendshipRequest entity) {
        Stream<FriendshipRequest> friendshipRequestStream = StreamSupport.stream(findAll().spliterator(), false);

        return friendshipRequestStream
                .filter(f -> f.equals(entity))
                .findFirst();
    }

    @Override
    public Iterable<FriendshipRequest> findAll() {
        Set<FriendshipRequest> friendshipRequests = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from FriendshipRequests");
            ResultSet resultSet = statement.executeQuery()
        ) {
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long from = resultSet.getLong("from");
                Long to = resultSet.getLong("to");
                String status = resultSet.getString("status");
                FriendshipRequest fr = new FriendshipRequest(from, to, status);
                fr.setId(id);
                friendshipRequests.add(fr);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendshipRequests;
    }

    public Optional<FriendshipRequest> save(FriendshipRequest entity) {
        Optional<FriendshipRequest> optionalFriendshipRequest = exists(entity);
        if(optionalFriendshipRequest.isPresent()) {
            return optionalFriendshipRequest;
        }
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into FriendshipRequests (id, \"from\", \"to\", status) values (?, ?, ?, ?)");
        ) {
            statement.setInt(1, Math.toIntExact(entity.getId()));
            statement.setInt(2, Math.toIntExact(entity.getFrom()));
            statement.setInt(3, Math.toIntExact(entity.getTo()));
            statement.setString(4, entity.getStatus());
            statement.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendshipRequest> delete(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("delete from FriendshipRequests where id = ?");
        ) {
            Optional<FriendshipRequest> optionalFriendshipRequest = findOne(id);
            if(optionalFriendshipRequest.isEmpty()) {
                return Optional.empty();
            }
            statement.setInt(1, Math.toIntExact(id));
            statement.executeUpdate();
            return optionalFriendshipRequest;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendshipRequest> update(FriendshipRequest entity) {
        Optional<FriendshipRequest> optionalFriendshipRequest = exists(entity);
        if(optionalFriendshipRequest.isEmpty()) {
            return Optional.of(entity);
        }
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("update FriendshipRequests set \"from\" = ?, \"to\" = ?, status = ? where id = ?");
        ) {
            statement.setInt(1, Math.toIntExact(entity.getFrom()));
            statement.setInt(2, Math.toIntExact(entity.getTo()));
            statement.setString(3, entity.getStatus());
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
            PreparedStatement statement = connection.prepareStatement("select id from FriendshipRequests");
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

    @Override
    public Iterable<FriendshipRequest> findAllFiltered(FilterDTO filter) {
        return null;
    }
}
