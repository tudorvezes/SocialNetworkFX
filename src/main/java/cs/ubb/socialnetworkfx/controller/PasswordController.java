package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.service.GeneralService;
import cs.ubb.socialnetworkfx.utils.Constants;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PasswordController {
    GeneralService generalService;
    User user;

    public PasswordField passwordField;
    public PasswordField confirmPasswordField;
    public Button setPasswordButton;
    public void setService(GeneralService generalService, User user, Stage stage) {
        this.generalService = generalService;
        this.user = user;

        stage.setTitle("FX | Set up password");
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);

        setPasswordButton.setDisable(true);
    }

    public void handlePasswordTyping() {
        String password = passwordField.getText();
        if(password.isEmpty()) {
            setPasswordButton.setDisable(true);
        }
        setPasswordButton.setDisable(false);
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
        handleConfirmPasswordTyping();
    }

    public void handleConfirmPasswordTyping() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if(confirmPassword.isEmpty()) {
            return;
        }
        if(!password.equals(confirmPassword)) {
            confirmPasswordField.setStyle("-fx-border-color: #b00d0d");
            setPasswordButton.setDisable(true);
        } else {
            confirmPasswordField.setStyle("-fx-border-color: #2daa2d");
            setPasswordButton.setDisable(false);
        }
    }

    public void handleSetPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if(password.equals(confirmPassword)) {
            generalService.addPassword(user, password);
            setPasswordButton.getScene().getWindow().hide();
        } else {
            AlertMessage.showErrorMessage(null, "Passwords don't match!");
        }
    }
}
