package cs.ubb.socialnetworkfx.domain;

import java.util.ArrayList;
import java.util.List;

public class User extends Entity<Long> {
    private String username;
    private String name;

    /**
     * Constructor for User with id
     * @param id Long - the id of the user
     * @param username String - the username of the user
     * @param name String - the name of the user
     */
    public User(Long id, String username, String name) {
        this.setId(id);
        this.username = username;
        this.name = name;
    }

    /**
     * Getter for the username
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Getter for the name
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for the username
     * @param username String - the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Setter for the name
     * @param name String - the new name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Converts the user to a string
     * @return the string
     */
    @Override
    public String toString() {
        return String.format("%d | %s | %s", this.getId(), username, name);
    }


    /**
     * Compares two users
     * @param o the object to be compared to
     * @return true if the users are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        return getUsername().equals(user.getUsername()) || getId().equals(user.getId());
    }

    /**
     * Generates the hashcode of the user
     * @return
     */
    @Override
    public int hashCode() {
        return this.username.hashCode();
    }

}