package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.domain.validator.ValidationException;
import cs.ubb.socialnetworkfx.service.AdminService;
import cs.ubb.socialnetworkfx.service.ServiceException;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class UpdateUserController {
    AdminService adminService;
    public TextField idField;
    public TextField usernameField;
    public TextField nameField;

    public void initialize() {
    }

    public void setService(AdminService adminService, Stage stage, User user) {
        this.adminService = adminService;
        if(user != null) {
            idField.setText(user.getId().toString());
            usernameField.setText(user.getUsername());
            nameField.setText(user.getName());
        }

        stage.setTitle("FX Admin Panel | Update User");
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);
    }

    public void handleUpdateUser() {
        if(idField.getText().isEmpty() || usernameField.getText().isEmpty() || nameField.getText().isEmpty()) {
            AlertMessage.showErrorMessage(null, "All fields must be completed!");
            return;
        }

        try {
            adminService.updateUser(Long.parseLong(idField.getText()), usernameField.getText(), nameField.getText());
            AlertMessage.showConfirmMessage(null, "User updated successfully!");
            Stage stage = (Stage) idField.getScene().getWindow();
            stage.close();
        } catch (ValidationException | ServiceException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleCancel() {
        Stage stage = (Stage) idField.getScene().getWindow();
        stage.close();
    }


}
