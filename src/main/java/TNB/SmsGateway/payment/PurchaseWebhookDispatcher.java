package TNB.SmsGateway.payment;

import TNB.SmsGateway.service.PurchaseService;
import org.springframework.stereotype.Component;

/**
 * Petit pont utilisé UNIQUEMENT par MockPaymentProvider pour simuler l'arrivée
 * d'un webhook, en passant par le même service (PurchaseService) qu'un vrai
 * webhook HTTP entrant. Garantit que le mock teste le vrai chemin de code.
 * ❌ À supprimer avec MockPaymentProvider quand un vrai provider est branché.
 */
@Component
public class PurchaseWebhookDispatcher {

    private final PurchaseService purchaseService;   // ✅ voir étape 62

    public PurchaseWebhookDispatcher(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    public void dispatchMockSuccess(String providerReference) {
        purchaseService.simulateWebhookEvent(providerReference + "|SUCCESS");   // ✅ renommé, un seul argument
    }
}