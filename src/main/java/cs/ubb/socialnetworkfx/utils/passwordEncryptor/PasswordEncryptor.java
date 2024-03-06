package cs.ubb.socialnetworkfx.utils.passwordEncryptor;

public interface PasswordEncryptor<T> {
    T encrypt(T password);
}
