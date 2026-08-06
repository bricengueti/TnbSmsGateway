package TNB.SmsGateway.exception.message;

import TNB.SmsGateway.exception.BusinessException;

public class QuotaExceededException extends BusinessException {
    public QuotaExceededException() {
        super("Quota mensuel de SMS atteint pour cette clé API. Contactez l'administrateur pour " +
                "augmenter votre limite.", "QUOTA_EXCEEDED", 429);
    }
}