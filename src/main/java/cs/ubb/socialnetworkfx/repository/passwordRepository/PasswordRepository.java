package cs.ubb.socialnetworkfx.repository.passwordRepository;

public interface PasswordRepository<ID> {
    boolean exists(ID id);
    void save(ID id, String password);
    void update(ID id, String oldPassword, String newPassword);
    boolean verify(ID id, String password);
}
