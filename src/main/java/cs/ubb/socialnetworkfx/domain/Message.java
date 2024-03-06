package cs.ubb.socialnetworkfx.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long> {
    User sender;
    Long chatRoomId;
    String content;
    LocalDateTime date;

    public Message(User sender, Long chatRoomId, String content, LocalDateTime date) {
        this.sender = sender;
        this.chatRoomId = chatRoomId;
        this.content = content;
        this.date = date;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getSender() {
        return sender;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
