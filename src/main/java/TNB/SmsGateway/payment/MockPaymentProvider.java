package TNB.SmsGateway.payment;

import TNB.SmsGateway.entity.PurchaseOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🧪 MOCK — simule un agrégateur de paiement (Campay/Fapshi/PawaPay...) pour
 * développer et tester tout le flux SANS compte marchand réel.
 *
 * Comportement simulé :
 * - initiatePayment() génère une fausse référence + une fausse URL de paiement
 * - après un délai (simule le temps que le client mette à payer sur son
 *   téléphone), le mock déclenche LUI-MÊME un faux webhook vers votre propre
 *   endpoint, exactement comme le ferait un vrai provider
 *
 * ❌ À SUPPRIMER (ou désactiver via profil) une fois un vrai provider branché.
 * 🔧 REMPLACEMENT : créez CampayPaymentProvider (ou autre) implements PaymentProvider,
 * copiez la signature des 4 méthodes ci-dessous, mais avec de vrais appels HTTP
 * vers l'API du provider (SDK ou RestTemplate/WebClient) au lieu des logs.
 */
@Service
@Primary   // ❌ à retirer quand un vrai provider devient la valeur par défaut
public class MockPaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentProvider.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final PurchaseWebhookDispatcher webhookDispatcher;   // ✅ voir étape 61

    public MockPaymentProvider(PurchaseWebhookDispatcher webhookDispatcher) {
        this.webhookDispatcher = webhookDispatcher;
    }

    @Override
    public PaymentInitiationResult initiatePayment(PurchaseOrder order) {
        String fakeReference = "MOCK_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String fakePaymentUrl = "https://mock-payment.local/pay/" + fakeReference;

        log.info("🧪 [MOCK] Paiement initié: ref={}, montant={}, user={}",
                fakeReference, order.getAmount(), order.getUser().getEmail());

        // 🔧 VRAI PROVIDER: ici, vous appelleriez l'API Campay/Fapshi pour créer
        // une session de paiement, et récupéreriez LEUR référence + LEUR URL réelle.
        // Ex (pseudo-code Campay): campayClient.collect(amount, phoneNumber) → { reference, ussd_code }

        // ✅ Simule le paiement réussi après 5 secondes (au lieu d'attendre un vrai humain)
        scheduler.schedule(() -> {
            log.info("🧪 [MOCK] Simulation du webhook de confirmation pour ref={}", fakeReference);
            webhookDispatcher.dispatchMockSuccess(fakeReference);
        }, 5, TimeUnit.SECONDS);

        return new PaymentInitiationResult(fakeReference, fakePaymentUrl);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        // 🧪 MOCK : accepte tout, aucune vérification.
        // 🔧 VRAI PROVIDER : vérifier ici une signature HMAC-SHA256 (ou équivalent)
        // calculée avec votre clé secrète, comparée au header envoyé par le provider.
        // Exemple générique :
        //   String expected = hmacSha256(rawBody, secretKey);
        //   return MessageDigest.isEqual(expected.getBytes(), signatureHeader.getBytes());
        return true;
    }

    @Override
    public WebhookPaymentEvent parseWebhookPayload(String rawBody) {
        // 🧪 MOCK : le format exact vient de PurchaseWebhookDispatcher.dispatchMockSuccess()
        // 🔧 VRAI PROVIDER : parser ICI le JSON réel envoyé par Campay/Fapshi (le format
        // diffère par provider — ex: Campay envoie {"reference":"...","status":"SUCCESSFUL"},
        // Fapshi envoie autre chose — à adapter précisément à leur doc webhook).
        // Format mock choisi : "reference|status" (simple, lisible en log)
        String[] parts = rawBody.split("\\|");
        String reference = parts[0];
        String status = parts.length > 1 ? parts[1] : "UNKNOWN";
        return new WebhookPaymentEvent(reference, "SUCCESS".equals(status), status);
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }
}