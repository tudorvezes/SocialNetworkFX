package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.validator.ValidationException;
import cs.ubb.socialnetworkfx.repository.RepositoryException;
import cs.ubb.socialnetworkfx.service.AdminService;
import cs.ubb.socialnetworkfx.service.GeneralService;
import cs.ubb.socialnetworkfx.service.ServiceException;
import cs.ubb.socialnetworkfx.service.UserService;
import cs.ubb.socialnetworkfx.utils.Constants;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUpController {
    GeneralService generalService;
    public TextField usernameField;
    public TextField nameField;
    public PasswordField passwordField;
    public PasswordField confirmPasswordField;
    public Button cancelButton;
    public Button singUpButton;

    public void setService(GeneralService generalService, Stage stage) {
        this.generalService = generalService;

        stage.setTitle("FX | Sign Up");
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);
    }

    public void handleTyping() {
        String username = usernameField.getText();
        if(username.isEmpty())
            return;
        if(generalService.validUsername(username)) {
            usernameField.setStyle("-fx-border-color: none");
            singUpButton.setDisable(false);
        } else {
            usernameField.setStyle("-fx-border-color: #b00d0d");
            singUpButton.setDisable(true);
        }
    }

    public void handlePasswordTyping() {
        String password = passwordField.getText();
        if(password.isEmpty()) {
            return;
        }
        int score = generalService.testPassword(password);
        if(score == Constants.WEAK_PASSWORD) {
            passwordField.setStyle("-fx-border-color: #b00d0d");
            //singUpButton.setDisable(true);
        } else if(score == Constants.MEDIUM_PASSWORD) {
            passwordField.setStyle("-fx-border-color: #f5b942");
            //singUpButton.setDisable(false);
        } else {
            passwordField.setStyle("-fx-border-color: #2daa2d");
            //singUpButton.setDisable(false);
        }
    }

    public void handleConfirmPasswordTyping() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if(confirmPassword.isEmpty()) {
            return;
        }
        if(!password.equals(confirmPassword)) {
            confirmPasswordField.setStyle("-fx-border-color: #b00d0d");
            singUpButton.setDisable(true);
        } else {
            confirmPasswordField.setStyle("-fx-border-color: #2daa2d");
            singUpButton.setDisable(false);
        }
    }

    public void handleSignUp() {
        String username = usernameField.getText();
        String name = nameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if(username.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertMessage.showErrorMessage(null, "Username, name and password must be completed!");
            return;
        }
        if(!password.equals(confirmPassword)) {
            AlertMessage.showErrorMessage(null, "Passwords don't match!");
            return;
        }
        try {
            generalService.addUser(username, name, password);

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/user-view.fxml"));

                Stage stage = new Stage();
                stage.setScene(new javafx.scene.Scene(loader.load()));

                UserService userService = generalService.getUserService();

                UserController controller = loader.getController();
                controller.setService(userService, generalService.logIn(username, password).get(), stage);

                stage.show();
            } catch (IOException e) {
                AlertMessage.showErrorMessage(null, e.getMessage());

            }
        } catch (ValidationException | RepositoryException | ServiceException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }

        Stage stage = (Stage) singUpButton.getScene().getWindow();
        stage.close();


    }

    public void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
