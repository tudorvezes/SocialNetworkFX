package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.service.UserService;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PostController {
    UserService userService;
    User currentUser;
    public TextArea contentArea;
    public Button postButton;
    public ProgressBar lengthBar;

    public void setService(UserService userService, User currentUser, Stage stage) {
        this.userService = userService;
        this.currentUser = currentUser;

        stage.setTitle("FX | Post | [" + currentUser.getUsername() +']');
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);
    }

    public void handlePost() {
        String content = contentArea.getText();
        userService.addPost(currentUser, content);
    }
}
