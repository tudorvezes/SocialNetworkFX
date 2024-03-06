package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.FriendshipRequest;
import cs.ubb.socialnetworkfx.domain.Post;
import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.service.UserService;
import cs.ubb.socialnetworkfx.utils.Constants;
import cs.ubb.socialnetworkfx.utils.events.ChangeEventType;
import cs.ubb.socialnetworkfx.utils.events.StatusChangeEvent;
import cs.ubb.socialnetworkfx.utils.observer.StatusObserver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class UserController implements StatusObserver {

    UserService userService;
    User currentUser;
    Optional<User> selectedUser;

    public Button isFriendButton;
    public Label requestSentLabel;
    public Button postButton;
    public AnchorPane postAnchorPane;
    public ScrollPane postScrollPane;
    public AnchorPane homeAnchorPane;
    public AnchorPane profileAnchorPane;
    public ScrollPane homeScrollPane;
    public ListView<StatusChangeEvent> notificationsList;
    public ListView<User> listView;
    public TextField searchField;
    public Label currentUserLabel;
    public Button acceptFriendshipButton;
    public Button sendFriendRequestButton;
    public Button declineFriendshipButton;
    public Button deleteFriendButton;
    public Label nameLabel;
    public Label usernameLabel;
    ObservableList<User> usersModel = FXCollections.observableArrayList();
    ObservableList<StatusChangeEvent> notificationsModel = FXCollections.observableArrayList();

    //for post pagination
    private int pageSize = 20;
    private int currentPage = 0;
    private int totalNoOfElements = 0;

    boolean homepage;

    private void setAllButton(Button button, boolean disable) {
        button.setDisable(disable);
        button.setVisible(!disable);
        button.mouseTransparentProperty().set(disable);
    }

    private void setAllLabel(Label label, boolean disable) {
        label.setDisable(disable);
        label.setVisible(!disable);
        label.mouseTransparentProperty().set(disable);
    }

    private void setButtonsFriend() {
        if(homepage) return;
        setAllButton(acceptFriendshipButton, true);
        setAllButton(declineFriendshipButton, true);
        setAllButton(deleteFriendButton, false);
        setAllButton(isFriendButton, false);
        setAllButton(sendFriendRequestButton, true);
        setAllLabel(requestSentLabel, true);
    }
    private void setButtonsNoFriend() {
        if(homepage) return;
        setAllButton(acceptFriendshipButton, true);
        setAllButton(declineFriendshipButton, true);
        setAllButton(deleteFriendButton, true);
        setAllButton(isFriendButton, true);
        setAllButton(sendFriendRequestButton, false);
        setAllLabel(requestSentLabel, true);
    }
    private void setButtonsPending() {
        if(homepage) return;
        setAllButton(acceptFriendshipButton, true);
        setAllButton(declineFriendshipButton, true);
        setAllButton(deleteFriendButton, true);
        setAllButton(isFriendButton, true);
        sendFriendRequestButton.setDisable(true);
        sendFriendRequestButton.setVisible(true);
        sendFriendRequestButton.mouseTransparentProperty().set(true);
        setAllLabel(requestSentLabel, false);
    }
    private void setButtonsReceived() {
        if(homepage) return;
        setAllButton(acceptFriendshipButton, false);
        setAllButton(declineFriendshipButton, false);
        setAllButton(deleteFriendButton, true);
        setAllButton(isFriendButton, true);
        setAllButton(sendFriendRequestButton, true);
        setAllLabel(requestSentLabel, true);
    }
    private void setButtonsSelf() {
        if(homepage) return;
        setAllButton(acceptFriendshipButton, true);
        setAllButton(declineFriendshipButton, true);
        setAllButton(deleteFriendButton, true);
        setAllButton(isFriendButton, true);
        setAllButton(sendFriendRequestButton, true);
        setAllLabel(requestSentLabel, true);
    }
    private void setStatus(int status) {
        if (status == Constants.NO_FRIENDSHIP) {
            setButtonsNoFriend();
        } else if (status == Constants.PENDING_REQUEST) {
            setButtonsPending();
        } else if (status == Constants.RECEIVED_REQUEST) {
            setButtonsReceived();
        } else if (status == Constants.ESTABLISHED_FRIENDSHIP) {
            setButtonsFriend();
        }
    }
    private void loadHomepage(boolean homepage) {
        this.homepage = homepage;
        if(homepage) {
            selectedUser = Optional.empty();

            postAnchorPane.setVisible(false);
            postAnchorPane.setDisable(true);
            postAnchorPane.mouseTransparentProperty().set(true);
            profileAnchorPane.setVisible(false);
            profileAnchorPane.setDisable(true);
            profileAnchorPane.mouseTransparentProperty().set(true);

            homeAnchorPane.setVisible(true);
            homeAnchorPane.setDisable(false);
            homeAnchorPane.mouseTransparentProperty().set(false);
        } else {
            postAnchorPane.setVisible(true);
            postAnchorPane.setDisable(false);
            postAnchorPane.mouseTransparentProperty().set(false);
            profileAnchorPane.setVisible(true);
            profileAnchorPane.setDisable(false);
            profileAnchorPane.mouseTransparentProperty().set(false);

            homeAnchorPane.setVisible(false);
            homeAnchorPane.setDisable(true);
            homeAnchorPane.mouseTransparentProperty().set(true);
        }
    }
    private void addNotification(StatusChangeEvent statusChangeEvent) {
        notificationsModel.add(0, statusChangeEvent);
    }
    private void initNotificationsModel() {
        Iterable<FriendshipRequest> friendshipRequests = userService.getAllFriendshipRequests(currentUser);
        List<StatusChangeEvent> statusChangeEvents = StreamSupport.stream(friendshipRequests.spliterator(), false)
                .map(friendshipRequest -> new StatusChangeEvent(ChangeEventType.REQUEST_FRIEND,
                        userService.getUserById(friendshipRequest.getFrom()).get(),
                        userService.getUserById(friendshipRequest.getTo()).get(), true))
                .toList();
        notificationsModel.setAll(statusChangeEvents);
    }

    private void loadProfile(User user) {
        loadHomepage(false);

        nameLabel.setText(user.getName());
        usernameLabel.setText("@" + user.getUsername());

        if(Objects.equals(user.getId(), currentUser.getId())) {
            setButtonsSelf();
        } else {
            setStatus(userService.getStatus(currentUser, user));
        }

        loadPosts();
    }


    public void setService(UserService userService, User currentUser, Stage stage) {
        this.userService = userService;
        this.currentUser = currentUser;

        currentUserLabel.setText(currentUser.getUsername());
        currentUserLabel.setAlignment(Pos.CENTER_RIGHT);

        userService.addObserver(this);
        initNotificationsModel();

        stage.setTitle("FX | [" + currentUser.getUsername() + "]");
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);

        listView.setVisible(false);
        listView.setDisable(true);
        listView.mouseTransparentProperty().set(true);

        acceptFriendshipButton.setAlignment(Pos.CENTER_LEFT);
        isFriendButton.setAlignment(Pos.CENTER_LEFT);
        sendFriendRequestButton.setAlignment(Pos.CENTER_LEFT);

        stage.setOnCloseRequest(event -> {
            userService.removeObserver(this);
            stage.close();
        });

        handleLoadCurrentUser();
    }

    @FXML
    public void initialize() {
        listView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    if(userService.isFriend(currentUser, item)) {
                        setText(item.getUsername() + " • Friends");
                    } else {
                        setText(item.getUsername());
                    }
                }
            }
        });

        notificationsList.setCellFactory(param -> new ListCell<StatusChangeEvent>() {
            @Override
            protected void updateItem(StatusChangeEvent item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label notificationLabel = new Label();
                    Label timeLabel = new Label();

                    if(item.getType() == ChangeEventType.ACCEPT_FRIEND) {
                        notificationLabel.setText(item.getFrom().getUsername() + " accepted your friend request!");
                    } else if(item.getType() == ChangeEventType.DECLINE_FRIEND) {
                        notificationLabel.setText(item.getFrom().getUsername() + " declined your friend request!");
                    } else if(item.getType() == ChangeEventType.REMOVE_FRIEND) {
                        notificationLabel.setText(item.getFrom().getUsername() + " removed you from friends!");
                    } else if(item.getType() == ChangeEventType.REQUEST_FRIEND) {
                        notificationLabel.setText(item.getFrom().getUsername() + " sent you a friend request!");
                    }

                    timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

                    notificationLabel.getStyleClass().add("notification-label");
                    timeLabel.getStyleClass().add("time-label");

                    Rectangle rectangle = new Rectangle(5, 10);
                    if(item.isSeen()) {
                        rectangle.setFill(Color.valueOf("#181818"));
                    } else {
                        rectangle.setFill(Color.RED);
                    }
                    VBox vbox = createVBox(notificationLabel, timeLabel);

                    HBox hbox = new HBox(rectangle, vbox);
                    hbox.setMargin(rectangle, new Insets(6,0,0,0));

                    setGraphic(hbox);
                }
            }

            private VBox createVBox(Label notificationLabel, Label timeLabel) {
                VBox vbox = new VBox(notificationLabel, timeLabel);
                VBox.setMargin(notificationLabel, new Insets(0, 0, 0, 5));
                VBox.setMargin(timeLabel, new Insets(0, 0, 0, 5));

                return vbox;
            }
        });

        listView.setItems(usersModel);
        notificationsList.setItems(notificationsModel);
    }

    public void handleSearch() {
        String username = searchField.getText();
        if (username.length() < 3) {
            listView.setVisible(false);
            listView.setDisable(true);
            listView.mouseTransparentProperty().set(true);
        } else {
            listView.setVisible(true);
            listView.setDisable(false);
            listView.mouseTransparentProperty().set(false);

            List<User> users = userService.searchByUsername(currentUser, username, 10);
            listView.setPrefHeight(users.size() * 27);
            usersModel.setAll(users);
        }
    }

    public void handleMessenger() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/messenger-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            MessengerController controller = loader.getController();
            controller.setService(userService, currentUser, stage);

            stage.show();
        } catch (Exception e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    public void handlePost() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/post-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            PostController controller = loader.getController();
            controller.setService(userService, currentUser, stage);

            stage.show();
        } catch (Exception e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleSendFriendRequest() {
        if (selectedUser.isEmpty()) {
            return;
        }

        userService.sendFriendshipRequest(currentUser, selectedUser.get());

        setButtonsPending();
    }

    public void handleAcceptFriendship() {
        if (selectedUser.isEmpty()) {
            return;
        }

        userService.acceptFriendshipRequest(currentUser, selectedUser.get());

        setButtonsFriend();
    }

    public void handleDeclineFriendship() {
        if (selectedUser.isEmpty()) {
            return;
        }

        userService.declineFriendshipRequest(currentUser, selectedUser.get());

        setButtonsNoFriend();
    }

    public void handleDeleteFriend() {
        if (selectedUser.isEmpty()) {
            return;
        }

        userService.removeFriend(currentUser, selectedUser.get());

        setButtonsNoFriend();
    }

    public void handleHomepage() {
        loadHomepage(true);
        loadPosts();
    }

    public void handleLoadCurrentUser() {
        selectedUser = Optional.of(currentUser);
        loadProfile(currentUser);
    }

    public void handleSelectUserSearch() {
        selectedUser = Optional.of((User) listView.getSelectionModel().getSelectedItem());

        searchField.clear();
        if (selectedUser.isEmpty()) return;

        listView.setVisible(false);
        listView.setDisable(true);
        listView.mouseTransparentProperty().set(true);

        loadProfile(selectedUser.get());
    }

    public void handleSelectNotification() {
        MultipleSelectionModel<StatusChangeEvent> selectionModel = notificationsList.getSelectionModel();
        StatusChangeEvent selectedNotification = selectionModel.getSelectedItem();

        if(selectedNotification == null) {
            return;
        }
        selectedNotification.setSeen(true);

        int selectedIndex = selectionModel.getSelectedIndex();
        notificationsModel.set(selectedIndex, selectedNotification);

        listView.setVisible(false);
        listView.setDisable(true);
        listView.mouseTransparentProperty().set(true);

        selectedUser = Optional.of(selectedNotification.getFrom());

        loadProfile(selectedUser.get());
    }

    private VBox createPost(Post post) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setSpacing(10);
        vbox.setMaxWidth(370);

        Optional<User> postUser = userService.getUserById(post.getUserId());
        if(postUser.isEmpty()) {
            return vbox;
        }
        Label usernameLabel = new Label(postUser.get().getUsername());
        usernameLabel.getStyleClass().add("notification-label");

        Label datestampLabel;
        LocalDateTime date = post.getDate();
        if(date.toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            datestampLabel = new Label(date.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else if (date.toLocalDate().equals(LocalDateTime.now().minusDays(1).toLocalDate())) {
            datestampLabel = new Label("yesterday " + date.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else if (date.getYear() == LocalDateTime.now().getYear()) {
            datestampLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd MMM HH:mm")));
        } else {
            datestampLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        }

        datestampLabel.getStyleClass().add("time-label");
        VBox headerVbox = new VBox(usernameLabel, datestampLabel);
        VBox.setMargin(datestampLabel, new Insets(-5, 0, 0, 0));


        Rectangle rectangle = new Rectangle(5, 27);
        rectangle.setFill(Color.valueOf("#181818"));

        HBox hbox = new HBox(rectangle, headerVbox);
        hbox.setMargin(rectangle, new Insets(6,0,0,0));
        hbox.setMargin(headerVbox, new Insets(0,0,0,5));

        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("content-label");
        contentLabel.setMaxWidth(350);
        contentLabel.setWrapText(true);

        vbox.getChildren().addAll(hbox, contentLabel);

        return vbox;
    }



    private void loadPosts() {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));

        if(homepage || selectedUser.isEmpty()) {
            List<Post> posts = userService.getFeed(currentUser);

            for(Post post : posts) {
                vbox.getChildren().add(createPost(post));
            }
            homeScrollPane.setContent(vbox);
        } else {
            List<Post> posts = userService.getPosts(selectedUser.get());
            for(Post post : posts) {
                vbox.getChildren().add(createPost(post));
            }
            postScrollPane.setContent(vbox);
        }
    }


    @Override
    public void update(StatusChangeEvent statusChangeEvent) {
        if(Objects.equals(statusChangeEvent.getTo().getUsername(), currentUser.getUsername())) {
            addNotification(statusChangeEvent);
            if(selectedUser.isEmpty()) {
                return;
            }

            if(selectedUser != null && Objects.equals(selectedUser.get().getUsername(), statusChangeEvent.getFrom().getUsername())) {
                if(statusChangeEvent.getType() == ChangeEventType.ACCEPT_FRIEND) {
                    setButtonsFriend();
                } else if(statusChangeEvent.getType() == ChangeEventType.DECLINE_FRIEND) {
                    setButtonsNoFriend();
                } else if(statusChangeEvent.getType() == ChangeEventType.REMOVE_FRIEND) {
                    setButtonsNoFriend();
                } else if(statusChangeEvent.getType() == ChangeEventType.REQUEST_FRIEND) {
                    setButtonsReceived();
                }
            }
        }
    }

}
