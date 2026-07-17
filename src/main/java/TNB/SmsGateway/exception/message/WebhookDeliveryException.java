package TNB.SmsGateway.exception.message;
import TNB.SmsGateway.exception.BusinessException;

import java.util.UUID;

public class WebhookDeliveryException extends BusinessException {

    public WebhookDeliveryException(UUID messageId, int attempt) {
        super("Échec de livraison webhook pour le message " + messageId +
                " (tentative " + attempt + ")", "WEBHOOK_DELIVERY_FAILED", 500);
    }


}