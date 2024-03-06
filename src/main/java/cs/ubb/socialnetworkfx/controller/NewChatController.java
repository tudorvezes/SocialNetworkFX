package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.service.ServiceException;
import cs.ubb.socialnetworkfx.service.UserService;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NewChatController {
    public TextField usernamesField;
    public TextField nameField;
    public Button createChatButton;
    UserService userService;
    User currentUser;

    public void setService(UserService userService, User currentUser, Stage stage) {
        this.userService = userService;
        this.currentUser = currentUser;

        stage.setTitle("FX | New Chat");
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);
    }

    public void handleGroup() {
        String usernames = usernamesField.getText();
        nameField.setDisable(!usernames.contains(","));
    }

    public void handleCreateChat() {
        String usernames = usernamesField.getText();
        String name = nameField.getText();
        if(usernames.isEmpty()) {
            AlertMessage.showErrorMessage(null, "Usernames must be completed!");
            return;
        }

        List<User> users = new ArrayList<>();
        for(String username : usernames.split(",")) {
            try {
                username = username.trim();
                Optional<User> user = userService.getUserByUsername(username);
                if(user.isPresent()) {
                    users.add(user.get());
                } else {
                    AlertMessage.showErrorMessage(null, "User " + username + " does not exist!");
                    return;
                }
            } catch (ServiceException e) {
                AlertMessage.showErrorMessage(null, e.getMessage());
                return;
            }
        }

        if(users.isEmpty()) {
            AlertMessage.showErrorMessage(null, "No valid usernames!");
            return;
        }
        try {
            userService.createChatRoom(name, currentUser, users);
            Stage stage = (Stage) createChatButton.getScene().getWindow();
            stage.close();
        } catch (ServiceException | IllegalArgumentException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

}


