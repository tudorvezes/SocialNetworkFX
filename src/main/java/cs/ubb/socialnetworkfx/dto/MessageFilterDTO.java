package cs.ubb.socialnetworkfx.dto;

import java.util.Optional;

public class MessageFilterDTO implements FilterDTO {
    private Optional<String> senderId = Optional.empty();
    private Optional<String> chatRoomId = Optional.empty();
    private Optional<String> content = Optional.empty();

    public void setSenderId(Long senderId) {
        this.senderId = Optional.of(senderId.toString());
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = Optional.of(chatRoomId.toString());
    }

    public void setContent(String content) {
        this.content = Optional.of(content);
    }

    public Optional<String> getSenderId() {
        return senderId;
    }

    public Optional<String> getChatRoomId() {
        return chatRoomId;
    }

    public Optional<String> getContent() {
        return content;
    }
}
