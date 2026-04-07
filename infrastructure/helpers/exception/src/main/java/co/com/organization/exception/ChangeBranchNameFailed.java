package co.com.organization.exception;

public class ChangeBranchNameFailed extends RuntimeException {
    public ChangeBranchNameFailed(String message) {
        super(message);
    }
}
