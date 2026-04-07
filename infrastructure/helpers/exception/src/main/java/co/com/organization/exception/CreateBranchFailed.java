package co.com.organization.exception;

public class CreateBranchFailed extends RuntimeException {
    public CreateBranchFailed(String message) {
        super(message);
    }
}
