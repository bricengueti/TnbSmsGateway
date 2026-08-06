package TNB.SmsGateway.payment;


/** Événement générique extrait d'un webhook, quel que soit le provider d'origine. */
public record WebhookPaymentEvent(
        String providerReference,
        boolean success,
        String rawStatus   // valeur brute du provider, gardée pour les logs/debug
) {}