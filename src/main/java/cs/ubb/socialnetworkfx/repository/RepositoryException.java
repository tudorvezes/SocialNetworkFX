package cs.ubb.socialnetworkfx.repository;

public class RepositoryException extends RuntimeException {
    public RepositoryException() {
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }

    public RepositoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * This method returns a string representation of the exception.
     * @return String, representing the exception.
     */
    @Override
    public String toString() {
        return "RepositoryException: " + getMessage();
    }
}