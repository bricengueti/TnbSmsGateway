package TNB.SmsGateway.exception.authentication;

import TNB.SmsGateway.exception.BusinessException;

public class InvalidOtpException extends BusinessException {

    public InvalidOtpException() {
        super("Code OTP invalide", "INVALID_OTP", 400);
    }

    public InvalidOtpException(String message) {
        super(message, "INVALID_OTP", 400);
    }
}