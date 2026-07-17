package TNB.SmsGateway.exception.message;
import TNB.SmsGateway.exception.BusinessException;

import java.util.UUID;

public class WebhookDeliveryException extends BusinessException {

    public WebhookDeliveryException(UUID messageId, int attempt) {
        super("Échec de livraison webhook pour le message " + messageId +
                " (tentative " + attempt + ")", "WEBHOOK_DELIVERY_FAILED", 500);
    }

    public static WebhookDeliveryException maxAttemptsReached(UUID messageId) {
        return new WebhookDeliveryException("Nombre max de tentatives atteint pour le message " + messageId,
                "WEBHOOK_MAX_ATTEMPTS", 500);
    }
}