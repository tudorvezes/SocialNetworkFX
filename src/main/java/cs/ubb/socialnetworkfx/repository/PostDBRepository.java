package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.Post;
import cs.ubb.socialnetworkfx.domain.validator.Validator;
import cs.ubb.socialnetworkfx.dto.PostFilterDTO;
import cs.ubb.socialnetworkfx.repository.paging.Page;
import cs.ubb.socialnetworkfx.repository.paging.Pageable;
import cs.ubb.socialnetworkfx.repository.paging.PagingRepository;
import cs.ubb.socialnetworkfx.utils.Pair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class PostDBRepository implements PagingRepository<Long, Post, PostFilterDTO> {
    private final String url;
    private final String user;
    private final String password;
    private final Validator<Post> validator;

    public PostDBRepository(String url, String user, String password, Validator<Post> validator) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<Post> findOne(Long aLong) {
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Posts WHERE id = ?")
        ) {
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                Long userId = resultSet.getLong("user_id");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                Post post = new Post(userId, content, datestamp);
                post.setId(aLong);
                return Optional.of(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Post> findAll() {
        Set<Post> posts = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Posts ORDER BY datestamp DESC");
        ) {
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userId = resultSet.getLong("user_id");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                Post post = new Post(userId, content, datestamp);
                post.setId(id);
                posts.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    @Override
    public Optional<Post> save(Post entity) {
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Posts(id, user_id, content, datestamp) VALUES (?, ?, ?, ?)");
        ) {
            statement.setLong(1, entity.getId());
            statement.setLong(2, entity.getUserId());
            statement.setString(3, entity.getContent());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            statement.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Post> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Post> update(Post entity) {
        return Optional.of(entity);
    }

    @Override
    public Set<Long> getAllKeys() {
        Set<Long> keys = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM Posts");
        ) {
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                keys.add(resultSet.getLong("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }

    private Pair<String, List<Long>> toSql(PostFilterDTO filter) {
        StringBuilder sql = new StringBuilder();
        List<Long> values = new ArrayList<>();

        if (filter.getUserIds().isPresent() && !filter.getUserIds().get().isEmpty()) {
            sql.append(" WHERE 1=1 AND Posts.user_id IN (");
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
    public Iterable<Post> findAllFiltered(PostFilterDTO postFilterDTO) {
        Set<Post> posts = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Posts " + toSql(postFilterDTO).getFirst() + " ORDER BY datestamp DESC");
        ) {
            List<Long> values = toSql(postFilterDTO).getSecond();
            for (int i = 0; i < values.size(); i++) {
                statement.setLong(i + 1, values.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userId = resultSet.getLong("user_id");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                Post post = new Post(userId, content, datestamp);
                post.setId(id);
                posts.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    @Override
    public Page<Post> findAllOnPage(Pageable pageable) {
        Set<Post> posts = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement pageStatement = connection.prepareStatement("SELECT * FROM Posts ORDER BY datestamp DESC LIMIT ? OFFSET ?");
            PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) AS count FROM Posts");
        ) {
            pageStatement.setInt(1, pageable.getPageSize());
            pageStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = pageStatement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userId = resultSet.getLong("user_id");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                Post post = new Post(userId, content, datestamp);
                post.setId(id);
                posts.add(post);
            }

            ResultSet countResultSet = countStatement.executeQuery();
            int count = 0;
            if(countResultSet.next()) {
                count = countResultSet.getInt("count");
            }

            return new Page<>(posts, count);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Post> findAllOnPageFiltered(Pageable pageable, PostFilterDTO postFilterDTO) {
        Set<Post> posts = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement pageStatement = connection.prepareStatement("SELECT * FROM Posts " + toSql(postFilterDTO).getFirst() + " ORDER BY datestamp DESC LIMIT ? OFFSET ?");
             PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) AS count FROM Posts " + toSql(postFilterDTO).getFirst());
        ) {
            List<Long> values = toSql(postFilterDTO).getSecond();
            for (int i = 0; i < values.size(); i++) {
                pageStatement.setLong(i + 1, values.get(i));
                countStatement.setLong(i + 1, values.get(i));
            }
            pageStatement.setInt(values.size() + 1, pageable.getPageSize());
            pageStatement.setInt(values.size() + 2, pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = pageStatement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userId = resultSet.getLong("user_id");
                String content = resultSet.getString("content");
                LocalDateTime datestamp = resultSet.getTimestamp("datestamp").toLocalDateTime();
                Post post = new Post(userId, content, datestamp);
                post.setId(id);
                posts.add(post);
            }

            ResultSet countResultSet = countStatement.executeQuery();
            int count = 0;
            if (countResultSet.next()) {
                count = countResultSet.getInt("count");
            }

            return new Page<>(posts, count);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
