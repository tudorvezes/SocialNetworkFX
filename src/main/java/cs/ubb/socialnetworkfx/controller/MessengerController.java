package cs.ubb.socialnetworkfx.controller;

import cs.ubb.socialnetworkfx.domain.ChatRoom;
import cs.ubb.socialnetworkfx.domain.Message;
import cs.ubb.socialnetworkfx.service.UserService;
import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.utils.Constants;
import cs.ubb.socialnetworkfx.utils.events.ChangeEventType;
import cs.ubb.socialnetworkfx.utils.events.ChatRoomChangeEvent;
import cs.ubb.socialnetworkfx.utils.observer.ChatRoomObserver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessengerController implements ChatRoomObserver {
    public ListView chatRoomsList;
    public Button createChatButton;
    public ScrollPane scrollPaneContainer;
    public TextField contentField;
    public Button sendButton;
    public ListView messagesList;
    UserService userService;
    User currentUser;
    ObservableList<ChatRoom> model = FXCollections.observableArrayList();
    ObservableList<Message> messagesModel = FXCollections.observableArrayList();
    ChatRoom selectedChatRoom = null;

    public void setService(UserService userService, User currentUser, Stage stage) {
        this.userService = userService;
        this.currentUser = currentUser;

        sendButton.setDisable(true);
        contentField.setDisable(true);

        userService.addObserver(this);
        stage.setOnCloseRequest(event -> {
            userService.removeObserver(this);
            stage.close();
        });

        stage.setTitle("FX | Messenger | [" + currentUser.getUsername() +']');
        Image image = new Image(getClass().getResourceAsStream("/cs/ubb/socialnetworkfx/icons/icon.png"));
        stage.getIcons().add(image);

        initChatRoomModel();
    }

    @FXML
    public void initialize() {
        chatRoomsList.setCellFactory(chatRoomsList -> new ListCell<ChatRoom>() {
            @Override
            protected void updateItem(ChatRoom chatRoom, boolean empty) {
                super.updateItem(chatRoom, empty);
                DateTimeFormatter todayFormatter = DateTimeFormatter.ofPattern("HH:mm");
                DateTimeFormatter otherFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                if (chatRoom == null || empty) {
                    setText(null);
                } else {
                    if (chatRoom.getType() == Constants.GROUP_CHAT) {
                        LocalDateTime lastMessageDate = chatRoom.getLastMessageDate();
                        LocalDateTime currentDate = LocalDateTime.now();

                        if (lastMessageDate.toLocalDate().equals(currentDate.toLocalDate())) {
                            setText(chatRoom.getName() + " - " + lastMessageDate.format(todayFormatter));
                        } else {
                            setText(chatRoom.getName() + " - " + lastMessageDate.format(otherFormatter));
                        }
                    } else {
                        LocalDateTime lastMessageDate = chatRoom.getLastMessageDate();
                        LocalDateTime currentDate = LocalDateTime.now();

                        List<User> users = chatRoom.getUsers().stream()
                                .filter(user -> !user.equals(currentUser))
                                .toList();
                        if (lastMessageDate.toLocalDate().equals(currentDate.toLocalDate())) {
                            setText(users.get(0).getUsername() + " - " + lastMessageDate.format(todayFormatter));
                        } else {
                            setText(users.get(0).getUsername() + " - " + lastMessageDate.format(otherFormatter));
                        }
                    }
                }
            }
        });
        messagesList.setCellFactory(messagesList -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);

                if (message == null || empty) {
                    setText(null);
                } else {
                    if(message.getSender().equals(currentUser))
                        setText("You: " + message.getContent());
                    else {
                        setText(message.getSender().getUsername() + ": " + message.getContent());
                    }
                }
            }
        });

        chatRoomsList.setItems(model);
        messagesList.setItems(messagesModel);
    }

    private void initChatRoomModel() {
        Iterable<ChatRoom> chatRooms = userService.getChatRooms(currentUser);
        List<ChatRoom> chatRoomList = StreamSupport.stream(chatRooms.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(chatRoomList);
        model.sort(Comparator.comparing(ChatRoom::getLastMessageDate).reversed());
    }

    public void handleNewChat() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cs/ubb/socialnetworkfx/views/new-chat-view.fxml"));

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));

            NewChatController newChatController = loader.getController();
            newChatController.setService(userService, currentUser, stage);

            stage.show();
        } catch (Exception e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleSendMessage() {
        if(selectedChatRoom == null) {
            sendButton.setDisable(true);
            return;
        }

        if(contentField.getText().isEmpty()) {
            AlertMessage.showErrorMessage(null, "You must write a message!");
            return;
        }

        try {
            userService.sendMessage(selectedChatRoom, currentUser, contentField.getText());
            contentField.clear();
            initChatRoomModel();
        } catch (Exception e) {
            AlertMessage.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleOpenChat() {
        selectedChatRoom = (ChatRoom) chatRoomsList.getSelectionModel().getSelectedItem();
        if (selectedChatRoom == null) {
            return;
        }

        sendButton.setDisable(false);
        contentField.setDisable(false);
        selectedChatRoom = userService.populateChatRoom(selectedChatRoom);
        Iterable<Message> messages = selectedChatRoom.getMessages();
        List<Message> messageList = StreamSupport.stream(messages.spliterator(), false)
                .toList();

        messagesModel.setAll(messageList);
        messagesModel.sort(Comparator.comparing(Message::getDate));
    }


    @Override
    public void update(ChatRoomChangeEvent event) {
        if(event.getFrom().equals(currentUser) && event.getType().equals(ChangeEventType.CREATED_CHATROOM)) {
            model.add(0, event.getChatRoom());
            return;
        }

        List<User> users = event.getTo();
        if(!users.contains(currentUser)) {
            return;
        }
        if(event.getType() == ChangeEventType.CREATED_CHATROOM) {
            model.add(0, event.getChatRoom());
        } else if(event.getType() == ChangeEventType.SENT_MESSAGE) {
            if(selectedChatRoom != null && selectedChatRoom.equals(event.getChatRoom())) {
                messagesModel.add(event.getMessage());
            }
        }
    }
}
