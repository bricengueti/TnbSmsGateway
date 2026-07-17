package TNB.SmsGateway.exception;

public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;

    // Constructeur avec 2 paramètres
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 400;
    }

    // Constructeur avec 3 paramètres
    public BusinessException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    // Constructeur avec 1 paramètre
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.statusCode = 400;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}