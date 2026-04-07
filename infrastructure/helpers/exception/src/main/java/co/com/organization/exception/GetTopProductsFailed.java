package co.com.organization.exception;

public class GetTopProductsFailed extends RuntimeException {
    public GetTopProductsFailed(String message) {
        super(message);
    }
}
