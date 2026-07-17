package TNB.SmsGateway.exception.device;
import TNB.SmsGateway.exception.BusinessException;

import java.util.UUID;

public class DeviceNotOnlineException extends BusinessException {

    public DeviceNotOnlineException(UUID deviceId) {
        super("Le device " + deviceId + " n'est pas en ligne", "DEVICE_NOT_ONLINE", 422);
    }
}