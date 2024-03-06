package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.domain.validator.Validator;
import cs.ubb.socialnetworkfx.dto.UserFilterDTO;
import cs.ubb.socialnetworkfx.repository.paging.Page;
import cs.ubb.socialnetworkfx.repository.paging.Pageable;
import cs.ubb.socialnetworkfx.repository.paging.PagingRepository;
import cs.ubb.socialnetworkfx.utils.Pair;

import java.sql.*;
import java.util.*;
import java.util.stream.StreamSupport;

public class UserDBRepository implements PagingRepository<Long, User, UserFilterDTO> {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<User> validator;

    public UserDBRepository(String url, String username, String password, Validator<User> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<User> findOne(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from Users " +
                    "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(id));
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");
                User u = new User(id, username, name);

                return Optional.of(u);
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from Users");
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");
                User u = new User(id, username, name);
                users.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    private Optional<User> exists(User user) {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setUsername(user.getUsername());

        Iterable<User> users = findAllFiltered(filter);
        return StreamSupport.stream(users.spliterator(), false)
                .filter(u -> u.equals(user))
                .findFirst();
    }

    @Override
    public Optional<User> save(User entity) {
        validator.validate(entity);
        Optional<User> optionalUser = exists(entity);
        if(optionalUser.isPresent()) {
            return optionalUser;
        }
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into Users (id, username, name) values (?,?,?)");
        ) {
            statement.setInt(1, Math.toIntExact(entity.getId()));
            statement.setString(2, entity.getUsername());
            statement.setString(3, entity.getName());
            statement.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("delete from Users where id = ?");
        ) {
            Optional<User> optionalUser = findOne(id);
            if(optionalUser.isEmpty()) {
                return Optional.empty();
            }
            statement.setInt(1, Math.toIntExact(id));
            statement.executeUpdate();
            return optionalUser;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        Optional<User> optionalUser = exists(entity);
        if(optionalUser.isEmpty()) {
            return Optional.of(entity);
        }
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("update Users set username = ?, name = ? where id = ?");
        ) {
            statement.setString(1, entity.getUsername());
            statement.setString(2, entity.getName());
            statement.setInt(3, Math.toIntExact(entity.getId()));
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
            PreparedStatement statement = connection.prepareStatement("select id from Users");
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                keys.add(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }

    private Pair<String, List<String>> toSQL(UserFilterDTO filter) {
        StringBuilder sql = new StringBuilder();
        List<String> values = new ArrayList<>();
        sql.append(" where 1=1");
        if (filter.getName().isPresent()) {
            sql.append(" and name like CONCAT('%', ?, '%')");
            values.add(filter.getName().get());
        }
        if (filter.getUsername().isPresent()) {
            sql.append(" and username like CONCAT('%', ?, '%')");
            values.add(filter.getUsername().get());
        }
        if(filter.getMaxSearch().isPresent()) {
            sql.append(" limit CAST(? AS INTEGER)");
            values.add(filter.getMaxSearch().get());
        }

        return new Pair<>(sql.toString(), values);
    }

    @Override
    public Iterable<User> findAllFiltered(UserFilterDTO filter) {
        Set<User> users = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from Users" +
                    toSQL(filter).getFirst());
        ) {
            List<String> values = toSQL(filter).getSecond();
            for (int i = 0; i < values.size(); i++) {
                statement.setString(i + 1, values.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");
                User u = new User(id, username, name);
                users.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    @Override
    public Page<User> findAllOnPage(Pageable pageable) {
        Set<User> users = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement pageStatement = connection.prepareStatement("select * from Users limit ? offset ?");
            PreparedStatement countStatement = connection.prepareStatement("select count(*) as count from Users");
        ) {
            pageStatement.setInt(1, pageable.getPageSize());
            pageStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());

            try(ResultSet pageResultSet = pageStatement.executeQuery();
                ResultSet countResultSet = countStatement.executeQuery();
            ) {
                int count = 0;
                if(countResultSet.next()) {
                    count = countResultSet.getInt("count");
                }
                while(pageResultSet.next()) {
                    Long id = pageResultSet.getLong("id");
                    String username = pageResultSet.getString("username");
                    String name = pageResultSet.getString("name");
                    User u = new User(id, username, name);
                    users.add(u);
                }
                return new Page<>(users, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<User> findAllOnPageFiltered(Pageable pageable, UserFilterDTO userFilterDTO) {
        Set<User> users = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement pageStatement = connection.prepareStatement("select * from Users" +
                    toSQL(userFilterDTO).getFirst() + " limit ? offset ?");
            PreparedStatement countStatement = connection.prepareStatement("select count(*) as count from Users" +
                    toSQL(userFilterDTO).getFirst());
        ) {
            List<String> values = toSQL(userFilterDTO).getSecond();
            for (int i = 0; i < values.size(); i++) {
                pageStatement.setString(i + 1, values.get(i));
                countStatement.setString(i + 1, values.get(i));
            }
            pageStatement.setInt(values.size() + 1, pageable.getPageSize());
            pageStatement.setInt(values.size() + 2, pageable.getPageSize() * pageable.getPageNumber());

            try(ResultSet pageResultSet = pageStatement.executeQuery();
                ResultSet countResultSet = countStatement.executeQuery();
            ) {
                int count = 0;
                if(countResultSet.next()) {
                    count = countResultSet.getInt("count");
                }
                while(pageResultSet.next()) {
                    Long id = pageResultSet.getLong("id");
                    String username = pageResultSet.getString("username");
                    String name = pageResultSet.getString("name");
                    User u = new User(id, username, name);
                    users.add(u);
                }
                return new Page<>(users, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
