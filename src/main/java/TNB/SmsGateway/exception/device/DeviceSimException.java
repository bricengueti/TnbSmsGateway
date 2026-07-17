package TNB.SmsGateway.exception.device;

import TNB.SmsGateway.exception.BusinessException;

public class DeviceSimException extends BusinessException {

    public DeviceSimException(String message) {
        super(message, "DEVICE_SIM_ERROR", 422);
    }

    public static DeviceSimException notFound() {
        return new DeviceSimException("SIM non trouvée");
    }

    public static DeviceSimException inactive() {
        return new DeviceSimException("La SIM est désactivée");
    }

    public static DeviceSimException quotaExceeded() {
        return new DeviceSimException("Quota quotidien dépassé");
    }
}