package TNB.SmsGateway.exception.message;


import TNB.SmsGateway.exception.BusinessException;

/** Le pack existe et a (ou avait) du solde, mais sa période de validité est dépassée. */
public class QuotaExpiredException extends BusinessException {
    public QuotaExpiredException() {
        super("Votre pack a expiré. Contactez l'administrateur ou achetez un nouveau pack pour continuer.",
                "QUOTA_EXPIRED", 403);
    }
}
