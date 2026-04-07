package co.com.organization.exception;

public class AddProductToBranchFailed extends RuntimeException {
    public AddProductToBranchFailed(String message) {
        super(message);
    }
}
