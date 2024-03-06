package cs.ubb.socialnetworkfx.utils.idGetter;

import java.sql.*;

public class IdGetterDB implements IdGetter<Long> {
    private final String url;
    private final String username;
    private final String password;

    public IdGetterDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Long getUniqueId() {
        Long newId = 0L;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT last_id FROM IdGetter")) {
            if (resultSet.next()) {
                newId = resultSet.getLong("last_id") + 1;
                statement.executeUpdate("UPDATE IdGetter SET last_id = " + newId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newId;
    }

    @Override
    public Long getUniqueId(Long expectedId) {
        Long newId = 0L;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT last_id FROM IdGetter")) {
            if (resultSet.next()) {
                newId = resultSet.getLong("last_id") + 1;
                if (newId < expectedId) {
                    newId = expectedId;
                }
                statement.executeUpdate("UPDATE IdGetter SET last_id = " + newId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newId;
    }

    @Override
    public Long getCurrentId() {
        Long newId = 0L;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT last_id FROM IdGetter")) {
            if (resultSet.next()) {
                newId = resultSet.getLong("last_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newId;
    }
}
