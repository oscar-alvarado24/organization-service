package co.com.organization.exception;

public class DeleteBranchFailed extends RuntimeException {
    public DeleteBranchFailed(String message) {
        super(message);
    }
}
