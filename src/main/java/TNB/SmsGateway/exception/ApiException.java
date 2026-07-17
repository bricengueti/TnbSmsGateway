package TNB.SmsGateway.exception;

public class ApiException extends RuntimeException {

    private final int status;
    private final String error;
    private final String message;

    public ApiException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
        this.message = message;
    }

    // Getters
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}