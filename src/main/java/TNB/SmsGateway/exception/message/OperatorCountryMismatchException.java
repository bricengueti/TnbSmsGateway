package TNB.SmsGateway.exception.message;

import TNB.SmsGateway.exception.BusinessException;

public class OperatorCountryMismatchException extends BusinessException {

    public OperatorCountryMismatchException(String operatorCode, String countryCode) {
        super("L'opérateur " + operatorCode + " n'appartient pas au pays " + countryCode,
                "OPERATOR_COUNTRY_MISMATCH", 400);
    }
}