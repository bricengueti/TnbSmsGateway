package TNB.SmsGateway.exception.device;

import TNB.SmsGateway.exception.BusinessException;

public class InvalidPairingCodeException extends BusinessException {

    public InvalidPairingCodeException() {
        super("Code de pairing invalide ou expiré", "INVALID_PAIRING_CODE", 400);
    }
}