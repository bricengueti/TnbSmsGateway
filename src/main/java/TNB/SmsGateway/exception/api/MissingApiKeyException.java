package TNB.SmsGateway.exception.api;

import TNB.SmsGateway.exception.BusinessException;

public class MissingApiKeyException extends BusinessException {

    public MissingApiKeyException() {
        super("Authorization header manquant", "MISSING_API_KEY", 401);
    }
}