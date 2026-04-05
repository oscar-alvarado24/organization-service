package co.com.organization.exception;

public class ValidateExistenceFailed extends RuntimeException {
    public ValidateExistenceFailed(String message) {
        super(message);
    }
}
