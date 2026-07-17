package TNB.SmsGateway.exception.api;

import TNB.SmsGateway.exception.BusinessException;

public class InsufficientScopeException extends BusinessException {

    public InsufficientScopeException() {
        super("Permissions insuffisantes pour cette action", "INSUFFICIENT_SCOPE", 403);
    }
}