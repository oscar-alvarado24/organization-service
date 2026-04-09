package co.com.organization.exception;

public class BranchAlreadyExistException extends RuntimeException {
    public BranchAlreadyExistException(String message) {
        super(message);
    }
}
