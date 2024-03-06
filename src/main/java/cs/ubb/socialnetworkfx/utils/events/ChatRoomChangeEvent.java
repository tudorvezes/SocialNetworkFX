package cs.ubb.socialnetworkfx.utils.events;

import cs.ubb.socialnetworkfx.domain.ChatRoom;
import cs.ubb.socialnetworkfx.domain.Message;
import cs.ubb.socialnetworkfx.domain.User;

import java.util.List;

public class ChatRoomChangeEvent implements Event {
    private ChangeEventType type;
    private User from;
    private List<User> to;
    private ChatRoom chatRoom;
    private Message message;

    public ChatRoomChangeEvent(ChangeEventType type, User from, List<User> to, ChatRoom chatRoom) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.chatRoom = chatRoom;
    }

    public ChatRoomChangeEvent(ChangeEventType type, User from, List<User> to, ChatRoom chatRoom, Message message) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.chatRoom = chatRoom;
        this.message = message;
    }

    public void setType(ChangeEventType type) {
        this.type = type;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public void setTo(List<User> to) {
        this.to = to;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public ChangeEventType getType() {
        return type;
    }

    public User getFrom() {
        return from;
    }

    public List<User> getTo() {
        return to;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public Message getMessage() {
        return message;
    }
}
