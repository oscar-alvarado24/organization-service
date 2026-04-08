package co.com.organization.exception;

public class SaveBranchFailed extends RuntimeException {
    public SaveBranchFailed(String message) {
        super(message);
    }
}
