package cs.ubb.socialnetworkfx.service;

public class FileReadingException extends RuntimeException {
    public FileReadingException() {
    }

    public FileReadingException(String message) {
        super(message);
    }

    public FileReadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileReadingException(Throwable cause) {
        super(cause);
    }

    public FileReadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * This method returns a string representation of the exception.
     * @return String, representing the exception.
     */
    @Override
    public String toString() {
        return "FileReadingException: " + getMessage();
    }
}