package cs.ubb.socialnetworkfx.domain;

import java.time.LocalDateTime;

public class Post extends Entity<Long> {
    private Long userId;
    private String message;
    private LocalDateTime date;

    /**
     * Constructor for the Post
     * @param message String - the message
     * @param date String - the date
     * @param userId Long - the userId
     */
    public Post(Long userId, String message, LocalDateTime date) {
        this.userId = userId;
        this.message = message;
        this.date = date;
    }


    /**
     * Getter for the message
     * @return the message
     */
    public String getContent() {
        return this.message;
    }

    /**
     * Getter for the date
     * @return the date
     */
    public LocalDateTime getDate() {
        return this.date;
    }

    /**
     * Getter for the userId
     * @return the userId
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Setter for the message
     * @param message String - the new message
     */
    public void setContent(String message) {
        this.message = message;
    }

    /**
     * Setter for the date
     * @param date String - the new date
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Setter for the userId
     * @param userId Long - the new userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Converts the post to a string
     * @return the string
     */
    @Override
    public String toString() {
        return "Post{" +
                "message='" + message + '\'' +
                ", date='" + date + '\'' +
                ", userId=" + userId +
                '}';
    }
}
