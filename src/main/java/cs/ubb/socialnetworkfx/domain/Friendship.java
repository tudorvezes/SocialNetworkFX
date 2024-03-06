package cs.ubb.socialnetworkfx.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Friendship extends Entity<Long> {
    private Long user1;
    private Long user2;
    private LocalDateTime datestamp;

    /**
     * Constructor for Friendship
     * @param user1 User - the first user
     * @param user2 User - the second user
     */
    public Friendship(User user1, User user2, LocalDateTime datestamp) {
        this.user1 = user1.getId();
        this.user2 = user2.getId();
        this.datestamp = datestamp;
    }

    /**
     * Constructor for Friendship with date
     * @param user1 User - the first user
     * @param user2 User - the second user
     * @param datestamp LocalDateTime - the date of the friendship
     */
    public Friendship(Long user1, Long user2, LocalDateTime datestamp) {
        this.user1 = user1;
        this.user2 = user2;
        this.datestamp = datestamp;
    }

    /**
     * Getter for the first user
     * @return the first user
     */
    public Long getUser1() {
        return this.user1;
    }

    /**
     * Getter for the second user
     * @return the second user
     */
    public Long getUser2() {
        return this.user2;
    }

    /**
     * Getter for the date
     * @return the date
     */
    public LocalDateTime getDate() {
        return this.datestamp;
    }

    /**
     * Converts the user into a string
     * @return the string
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return this.getId() + " | " + user1 + " + " + user2 + " (on " + datestamp.format(formatter) + ")";
    }

    /**
     * Checks if two friendships are equal
     * @param o the object to be compared to
     * @return true if the friendships are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship friendship)) return false;

        return (getUser1().equals(friendship.getUser1()) && getUser2().equals(friendship.getUser2())) ||
                (getUser1().equals(friendship.getUser2()) && getUser2().equals(friendship.getUser1()));
    }


}

