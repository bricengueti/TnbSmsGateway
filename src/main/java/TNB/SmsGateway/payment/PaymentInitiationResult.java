package TNB.SmsGateway.payment;

/** Résultat retourné juste après avoir initié un paiement chez le provider. */
public record PaymentInitiationResult(
        String providerReference,   // référence unique côté provider, à surveiller
        String paymentUrl           // URL à ouvrir/rediriger le client pour payer
) {}