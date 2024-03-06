package cs.ubb.socialnetworkfx.config;

public class DatabaseConnectionConfig {
    private static String passwordProtection(String input) {
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                char shiftedChar = (char) (chars[i] - 2);
                if ((Character.isLowerCase(chars[i]) && shiftedChar < 'a') ||
                        (Character.isUpperCase(chars[i]) && shiftedChar < 'A')) {
                    shiftedChar = (char) (chars[i] + 24);
                }
                chars[i] = shiftedChar;
            }
        }

        return new String(chars);
    }

    public static final String URL = "jdbc:postgresql://localhost:5432/SocialNetwork";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = passwordProtection("Ucogtigo112");

}
