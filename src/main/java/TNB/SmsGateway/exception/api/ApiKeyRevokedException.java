package TNB.SmsGateway.exception.api;

import TNB.SmsGateway.exception.BusinessException;

public class ApiKeyRevokedException extends BusinessException {

    public ApiKeyRevokedException() {
        super("Cette clé API a été révoquée", "API_KEY_REVOKED", 401);
    }
}