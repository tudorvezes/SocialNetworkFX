package cs.ubb.socialnetworkfx.domain;

import cs.ubb.socialnetworkfx.utils.Constants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ChatRoom extends Entity<Long> {
    private String name;
    private List<User> users;
    private Iterable<Message> messages;
    private LocalDateTime lastMessageDate = null;
    int type = Constants.PRIVATE_CHAT;

    public ChatRoom(String name, List<User> users) {
        this.name = name;
        this.users = users;
        this.messages = new ArrayList<>();
        this.lastMessageDate = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }

    public Iterable<Message> getMessages() {
        return messages;
    }

    public LocalDateTime getLastMessageDate() {
        return lastMessageDate;
    }

    public int getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setMessages(Iterable<Message> messages) {
        this.messages = messages;
    }

    public void setLastMessageDate(LocalDateTime lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChatRoom chatRoom) {
            if(Objects.equals(this.id, chatRoom.getId())) {
                return true;
            }
            return new HashSet<>(this.users).containsAll(chatRoom.getUsers()) && new HashSet<>(chatRoom.getUsers()).containsAll(this.users);
        }
        return false;
    }
}
