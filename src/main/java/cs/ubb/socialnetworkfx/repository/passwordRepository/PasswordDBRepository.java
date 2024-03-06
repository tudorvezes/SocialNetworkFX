package cs.ubb.socialnetworkfx.repository.passwordRepository;


import cs.ubb.socialnetworkfx.repository.RepositoryException;
import cs.ubb.socialnetworkfx.utils.passwordEncryptor.PasswordEncryptor;

import java.sql.*;

public class PasswordDBRepository implements PasswordRepository<Long> {
    private String url;
    private String username;
    private String password;
    private PasswordEncryptor<String> passwordEncryptor;

    public PasswordDBRepository(String url, String username, String password, PasswordEncryptor<String> passwordEncryptor) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public boolean exists(Long aLong) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM Passwords WHERE user_id = ?")
        ) {
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Long aLong, String userPassword) {
        if(exists(aLong)) {
            throw new RepositoryException("Password already exists in database!");
        }
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO Passwords(user_id, password_hash) VALUES (?, ?)")
        ) {
            statement.setLong(1, aLong);
            statement.setString(2, passwordEncryptor.encrypt(aLong + userPassword));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void update(Long aLong, String oldPassword, String newPassword) {
        if(verify(aLong, aLong + oldPassword)) {
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 PreparedStatement statement = connection.prepareStatement("UPDATE Passwords SET password_hash = ? WHERE user_id = ?")
            ) {
                statement.setString(1, passwordEncryptor.encrypt(aLong + newPassword));
                statement.setLong(2, aLong);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Wrong password!");
        }

    }

    @Override
    public boolean verify(Long aLong, String userPassword) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT password_hash FROM Passwords WHERE user_id = ?")
        ) {
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String passwordHash = resultSet.getString("password_hash");
                return passwordEncryptor.encrypt(aLong + userPassword).equals(passwordHash);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new RepositoryException("User not found!");
    }
}
