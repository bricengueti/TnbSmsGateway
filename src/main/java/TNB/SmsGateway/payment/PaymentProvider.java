package TNB.SmsGateway.payment;


import TNB.SmsGateway.entity.PurchaseOrder;

/**
 * INTERFACE DE PAIEMENT — point d'abstraction unique.
 *
 * Chaque agrégateur (Campay, Fapshi, PawaPay, Stripe...) aura sa propre
 * implémentation de cette interface. Le reste de l'application (PurchaseService,
 * le webhook controller) ne connaît QUE cette interface, jamais un provider
 * concret directement.
 *
 * 🔧 QUAND VOUS AUREZ UN VRAI COMPTE PROVIDER :
 * 1. Créez une nouvelle classe, ex: CampayPaymentProvider implements PaymentProvider
 * 2. Annotez-la @Service + @ConditionalOnProperty ou @Primary selon votre config
 * 3. Retirez @Primary de MockPaymentProvider (ou supprimez-le du contexte dev)
 * Rien d'autre à changer — PurchaseService et le webhook restent identiques.
 */
public interface PaymentProvider {

    /**
     * Initie un paiement chez le provider — retourne l'URL où rediriger le
     * client pour payer, et la référence à surveiller pour la confirmation.
     */
    PaymentInitiationResult initiatePayment(PurchaseOrder order);

    /**
     * Vérifie que le webhook reçu provient bien du provider (signature HMAC,
     * clé API dans un header, etc. — dépend du provider). RETOURNE TOUJOURS
     * true CÔTÉ MOCK, mais un vrai provider doit vérifier une vraie signature.
     */
    boolean verifyWebhookSignature(String rawBody, String signatureHeader);

    /**
     * Parse le corps du webhook (format spécifique à chaque provider) en un
     * événement générique exploitable par PurchaseService.
     */
    WebhookPaymentEvent parseWebhookPayload(String rawBody);

    /** Identifiant du provider, utilisé pour router les webhooks entrants. */
    String getProviderName();
}