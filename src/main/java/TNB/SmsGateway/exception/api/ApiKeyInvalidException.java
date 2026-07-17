package TNB.SmsGateway.exception.api;


import TNB.SmsGateway.exception.BusinessException;

public class ApiKeyInvalidException extends BusinessException {

    public ApiKeyInvalidException() {
        super("Clé API invalide", "INVALID_API_KEY", 401);
    }

    public ApiKeyInvalidException(String format) {
        super("Format de clé API invalide: " + format, "INVALID_API_KEY_FORMAT", 401);
    }
}