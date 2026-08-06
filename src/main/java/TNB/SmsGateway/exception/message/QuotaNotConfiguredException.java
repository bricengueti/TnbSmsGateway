package TNB.SmsGateway.exception.message;

import TNB.SmsGateway.exception.BusinessException;

public class QuotaNotConfiguredException extends BusinessException {
    public QuotaNotConfiguredException() {
        super("Aucun quota configuré pour cette clé API en mode pool partagé. Contactez " +
                "l'administrateur pour activer votre accès.", "QUOTA_NOT_CONFIGURED", 403);
    }
}