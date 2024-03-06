package cs.ubb.socialnetworkfx.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AlertMessage {
    public static void showMessage(Stage owner, Alert.AlertType type, String header, String text) {
        Alert message=new Alert(type);
        message.setHeaderText(header);
        message.setContentText(text);
        message.initOwner(owner);
        message.showAndWait();
    }

    public static void showErrorMessage(Stage owner, String text) {
        Alert message=new Alert(Alert.AlertType.ERROR);
        message.initOwner(owner);
        message.setTitle("Error!");
        message.setContentText(text);
        message.showAndWait();
    }

    public static void showInfoMessage(Stage owner, String text) {
        Alert message=new Alert(Alert.AlertType.INFORMATION);
        message.initOwner(owner);
        message.setTitle("Info");
        message.setContentText(text);
        message.showAndWait();
    }

    public static void showWarningMessage(Stage owner, String text) {
        Alert message=new Alert(Alert.AlertType.WARNING);
        message.initOwner(owner);
        message.setTitle("Warning");
        message.setContentText(text);
        message.showAndWait();
    }

    public static void showIdMismatchWarning(Stage owner, String expectedId, String actualId) {
        Alert message = new Alert(Alert.AlertType.WARNING);
        message.initOwner(owner);
        message.setTitle("ID Mismatch Warning");
        message.setHeaderText("User added with a different ID!");
        message.setContentText("Expected ID: " + expectedId + "\nActual ID: " + actualId);
        message.showAndWait();
    }

    public static void showConfirmMessage(Stage owner, String text) {
        Alert message=new Alert(Alert.AlertType.CONFIRMATION);
        message.initOwner(owner);
        message.setTitle("Success!");
        message.setContentText(text);
        message.showAndWait();
    }


}
