package TNB.SmsGateway.exception.device;

import TNB.SmsGateway.exception.BusinessException;

public class DeviceLimitExceededException extends BusinessException {
    public DeviceLimitExceededException(int maxDevices) {
        super("Limite de " + maxDevices + " device(s) atteinte pour votre pack. " +
                "Contactez l'administrateur pour l'augmenter.", "DEVICE_LIMIT_EXCEEDED", 403);
    }
}