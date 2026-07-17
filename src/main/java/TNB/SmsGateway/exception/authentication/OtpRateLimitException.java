package TNB.SmsGateway.exception.authentication;

import TNB.SmsGateway.exception.BusinessException;

public class OtpRateLimitException extends BusinessException {

    public OtpRateLimitException() {
        super("Trop de tentatives. Veuillez patienter 10 minutes", "TOO_MANY_OTP_REQUESTS", 429);
    }
}