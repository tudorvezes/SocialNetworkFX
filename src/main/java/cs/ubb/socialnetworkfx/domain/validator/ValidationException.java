package cs.ubb.socialnetworkfx.domain.validator;

public class ValidationException extends RuntimeException {
    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * This method returns a string representation of the exception.
     * @return String, representing the exception.
     */
    @Override
    public String toString() {
        return "ValidationException: " + getMessage();
    }
}