package co.com.organization.exception;

public class CreateProductFailed extends RuntimeException {
    public CreateProductFailed(String message) {
        super(message);
    }
}
