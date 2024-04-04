package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.domain.validator.ValidationException;
import cs.ubb.socialnetworkfx.repository.paging.Page;
import cs.ubb.socialnetworkfx.repository.paging.Pageable;
import cs.ubb.socialnetworkfx.service.AdminService;
import cs.ubb.socialnetworkfx.service.ServiceException;
import cs.ubb.socialnetworkfx.utils.events.ChangeEventType;
import cs.ubb.socialnetworkfx.utils.events.UserChangeEvent;
import cs.ubb.socialnetworkfx.utils.observer.Observer;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AdminController implements Observer<UserChangeEvent> {
    AdminService adminService;
    ObservableList<User> model = FXCollections.observableArrayList();

    @FXML
    public TableView<User> userTable;
    @FXML
    public TableColumn<User, String> idColumn;
    @FXML
    public TableColumn<User, String> usernameColumn;
    @FXML
    public TableColumn<User, String> nameColumn;
    @FXML
    public TextField idField;
    @FXML
    public TextField usernameField;
    @FXML
    public TextField nameField;
    @FXML
    public Button addButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button updateButton;
    @FXML
    public Button nextIdButton;
    @FXML
    public Button clearButton;
    @FXML
    public Button previousButton;
    @FXML
    public Button nextButton;
    @FXML
    public Label currentPageNo;
    @FXML
    public ChoiceBox<Integer> elementsOnPageChoiceBox;

    private int pageSize = 5;
    private int currentPage = 0;
    private int totalNrOfElems = 0;

    public void setService(AdminService adminService, Stage stage) {
        this.adminService = adminService;
        adminService.addObserver(this);
        initModel();

        elementsOnPageChoiceBox.getItems().addAll(5, 10, 20, 30);
        elementsOnPageChoiceBox.setValue(5);

        stage.setOnCloseRequest(event -> {
            adminService.removeObserver(this);
            stage.close();
        });

        stage.setTitle("FX Admin Panel");
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);

    }



    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<User, String>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("name"));
        userTable.setItems(model);
    }

    private void initModel() {
        Page<User> page = adminService.getAllUsers(new Pageable(currentPage, pageSize));
        int maxPage = (int) Math.ceil((double) page.getTotalNoOfElems() / pageSize) - 1;

        if (currentPage > maxPage) {
            currentPage = maxPage;
            page = adminService.getAllUsers(new Pageable(currentPage, pageSize));
        }

        model.setAll(StreamSupport.stream(page.getElementsOnPage().spliterator(), false).collect(Collectors.toList()));
        totalNrOfElems = page.getTotalNoOfElems();

        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage + 1) * pageSize >= totalNrOfElems);
        currentPageNo.setText(String.valueOf(currentPage + 1) + "/" + String.valueOf((int) Math.ceil((double) totalNrOfElems / pageSize)));
    }

    @FXML
    public void handlePrevious() {
        currentPage--;
        initModel();
        currentPageNo.setText(String.valueOf(currentPage + 1) + "/" + String.valueOf((int) Math.ceil((double) totalNrOfElems / pageSize)));
    }

    @FXML
    public void handleNext() {
        currentPage++;
        initModel();
        currentPageNo.setText(String.valueOf(currentPage + 1) + "/" + String.valueOf((int) Math.ceil((double) totalNrOfElems / pageSize)));
    }

    @FXML
    public void handleElementsOnPage() {
        pageSize = elementsOnPageChoiceBox.getValue();
        currentPage = 0;
        initModel();
        currentPageNo.setText(String.valueOf(currentPage + 1) + "/" + String.valueOf((int) Math.ceil((double) totalNrOfElems / pageSize)));
    }

    @FXML
    public void handleAdd() {
        if(idField.getText().isEmpty() || usernameField.getText().isEmpty() || nameField.getText().isEmpty()){
            AlertMessage.showErrorMessage(null, "All fields must be completed!");
            return;
        }
        if (!idField.getText().matches("\\d+")) {
            AlertMessage.showErrorMessage(null, "ID must contain only digits!");
            return;
        }

        Long id = Long.parseLong(idField.getText());
        String username = usernameField.getText();
        String name = nameField.getText();
        try {
            Optional<User> user = adminService.addUser(id, username, name);
            user.ifPresent(value -> userTable.getSelectionModel().select(value));
            if(user.isPresent()) {
                if(Objects.equals(user.get().getId(), id)) {
                    AlertMessage.showConfirmMessage(null, "User added successfully!");
                } else {
                    AlertMessage.showIdMismatchWarning(null, id.toString(), user.get().getId().toString());
                }
            }
        } catch (ServiceException | ValidationException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        User selected = (User) userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            adminService.removeUserByID(selected.getId());
            handleClear();
        } else {
            AlertMessage.showErrorMessage(null, "No user selected!");
        }
    }

    @FXML
    public void handleUpdate() {
        User selectedUser = (User) userTable.getSelectionModel().getSelectedItem();
        if(selectedUser == null) {
            AlertMessage.showErrorMessage(null, "No user selected!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/update-user-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            UpdateUserController controller = loader.getController();
            controller.setService(adminService, stage, selectedUser);

            stage.show();

            stage.setOnHiding(event -> {
                List<User> userList = StreamSupport.stream(adminService.getAllUsers().spliterator(), false)
                        .filter(user -> Objects.equals(user.getId(), selectedUser.getId()))
                        .collect(Collectors.toList());
                if(userList.size() == 1) {
                    userTable.getSelectionModel().select(userList.get(0));
                    idField.setText(userList.get(0).getId().toString());
                    usernameField.setText(userList.get(0).getUsername());
                    nameField.setText(userList.get(0).getName());
                }
            });

        } catch (IOException e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleSelection() {
        User selected = (User) userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            idField.setText(selected.getId().toString());
            usernameField.setText(selected.getUsername());
            nameField.setText(selected.getName());
        }
    }

    @FXML
    public void handleClear() {
        idField.setText("");
        usernameField.setText("");
        nameField.setText("");
        userTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleNextId() {
        Long nextId = adminService.getNextId();
        idField.setText(nextId.toString());
    }


    @Override
    public void update(UserChangeEvent userChangeEvent) {
        if(userChangeEvent.getType() == ChangeEventType.ADD) {
            initModel();
        }
        if(userChangeEvent.getType() == ChangeEventType.DELETE) {
            initModel();
        }
        if(userChangeEvent.getType() == ChangeEventType.UPDATE) {
            model.remove(userChangeEvent.getOldData());
            model.add(userChangeEvent.getData());
        }
    }
}
