package TNB.SmsGateway.exception.message;

import TNB.SmsGateway.exception.BusinessException;

public class WebhookException extends BusinessException {

    public WebhookException(String message) {
        super(message, "WEBHOOK_ERROR", 400);
    }

    public static WebhookException urlNotConfigured() {
        return new WebhookException("URL de webhook non configurée");
    }

    public static WebhookException invalidUrl(String url) {
        return new WebhookException("URL de webhook invalide: " + url);
    }
}