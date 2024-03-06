package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.service.AdminService;
import cs.ubb.socialnetworkfx.service.GeneralService;
import cs.ubb.socialnetworkfx.service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    GeneralService generalService;
    public Button closeButton;
    public PasswordField passwordField;
    public TextField usernameField;
    public Button loginButton;
    public Button adminButton;

    public void setService(GeneralService generalService, Stage stage) {
        this.generalService = generalService;

        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        stage.getScene().getStylesheets().add("cs/ubb/socialnetworkfx/style/general-style.css");
        stage.setTitle("FX | Login");
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);
    }

    private boolean firstTimeLogin(String username) {
        Optional<User> optionalUser = generalService.hasPassword(username);
        if(optionalUser.isEmpty()) {
            return false;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/password-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            PasswordController controller = loader.getController();
            controller.setService(generalService, optionalUser.get(), stage);

            stage.show();
        } catch (IOException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }

        return true;
    }

    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if(username.isEmpty() || password.isEmpty()) {
            AlertMessage.showErrorMessage(null, "Username and password must be completed!");
            return;
        }
        Optional<User> user = generalService.logIn(username, password);
        if(user.isEmpty()) {
            boolean firstTime = firstTimeLogin(username);
            if(firstTime) {
                return;
            }
            AlertMessage.showErrorMessage(null, "Username or password don't match!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/user-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            UserService userService = generalService.getUserService();

            UserController controller = loader.getController();
            controller.setService(userService, user.get(), stage);

            stage.show();
        } catch (IOException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleAdmin() {
        try {
            AdminService adminService = generalService.getAdminService();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/admin-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            AdminController controller = loader.getController();
            controller.setService(adminService, stage);

            stage.show();
        } catch (IOException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/signup-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            SignUpController controller = loader.getController();
            controller.setService(generalService, stage);

            stage.show();
        } catch (IOException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }
}
