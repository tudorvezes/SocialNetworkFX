package cs.ubb.socialnetworkfx.utils.passwordEncryptor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHashEncryptor implements PasswordEncryptor<String> {
    @Override
    public String encrypt(String password) {
        try {
            // creaza o instanta MessageDigest cu algoritmul SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // adauga bitii parolei
            md.update(password.getBytes());

            // bitii hashuiti/ criptati
            byte[] bytes = md.digest();

            // converteste bytes[] in format hexazecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
