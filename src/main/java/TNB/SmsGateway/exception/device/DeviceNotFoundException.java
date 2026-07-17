package TNB.SmsGateway.exception.device;
import TNB.SmsGateway.exception.BusinessException;

import java.util.UUID;

public class DeviceNotFoundException extends BusinessException {

    public DeviceNotFoundException(UUID deviceId) {
        super("Device non trouvé: " + deviceId, "DEVICE_NOT_FOUND", 404);
    }
}