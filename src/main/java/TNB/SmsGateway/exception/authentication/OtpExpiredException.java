package TNB.SmsGateway.exception.authentication;


import TNB.SmsGateway.exception.BusinessException;

public class OtpExpiredException extends BusinessException {

    public OtpExpiredException() {
        super("Code OTP expiré. Veuillez en demander un nouveau", "OTP_EXPIRED", 400);
    }
}