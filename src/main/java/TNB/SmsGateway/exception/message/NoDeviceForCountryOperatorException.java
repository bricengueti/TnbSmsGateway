package TNB.SmsGateway.exception.message;

import TNB.SmsGateway.exception.BusinessException;

public class NoDeviceForCountryOperatorException extends BusinessException {

    public NoDeviceForCountryOperatorException(String countryCode, String operatorCode) {
        super("Aucun device disponible pour le couple " + countryCode + "/" + operatorCode,
                "NO_DEVICE_FOR_COUNTRY_OPERATOR", 422);
    }
}