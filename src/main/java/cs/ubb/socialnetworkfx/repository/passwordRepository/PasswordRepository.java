package cs.ubb.socialnetworkfx.repository.passwordRepository;

public interface PasswordRepository<ID> {
    /**
     * This method checks if a password exists for a specific id.
     * @param id ID - the id
     * @return boolean - true if the password exists, false otherwise
     */
    boolean exists(ID id);

    /**
     * This method saves a password for a specific id.
     * @param id ID - the id
     * @param password String - the password
     */
    void save(ID id, String password);

    /**
     * This method deletes a password for a specific id.
     * @param id ID - the id
     */
    void update(ID id, String oldPassword, String newPassword);

    /**
     * This method deletes a password for a specific id.
     * @param id ID - the id
     */
    boolean verify(ID id, String password);
}
