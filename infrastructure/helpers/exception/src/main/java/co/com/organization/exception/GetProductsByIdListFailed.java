package co.com.organization.exception;

public class GetProductsByIdListFailed extends RuntimeException {
    public GetProductsByIdListFailed(String message) {
        super(message);
    }
}
