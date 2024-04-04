package cs.ubb.socialnetworkfx.utils.passwordEncryptor;

public interface PasswordEncryptor<T> {
    /**
     * Encrypts the given password.
     * @param password T - the password to be encrypted
     * @return T - the encrypted password
     */
    T encrypt(T password);
}
