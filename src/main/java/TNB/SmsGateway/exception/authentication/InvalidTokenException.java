package TNB.SmsGateway.exception.authentication;



import TNB.SmsGateway.exception.BusinessException;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super("Token JWT invalide ou expiré", "INVALID_TOKEN", 401);
    }

    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN", 401);
    }
}