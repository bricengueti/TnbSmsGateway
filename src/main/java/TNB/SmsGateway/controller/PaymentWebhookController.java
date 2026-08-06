package TNB.SmsGateway.controller;

import TNB.SmsGateway.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Endpoint PUBLIC (pas de JWT/clé API) — les providers de paiement n'ont pas
 * de session utilisateur, la sécurité passe entièrement par la vérification
 * de signature faite dans PurchaseService.handleWebhookEvent().
 * 🔧 À déclarer dans SecurityConfig comme .permitAll(), ex: "/v1/webhook/payment/**"
 */
@RestController
@RequestMapping("/v1/webhook/payment")
@Tag(name = "Webhook Paiement", description = "Réception des confirmations de paiement (provider externe)")
public class PaymentWebhookController {

    private final PurchaseService purchaseService;

    public PaymentWebhookController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Operation(summary = "Webhook de confirmation de paiement", description = "Appelé par le provider " +
            "de paiement (Campay, Fapshi...) après un paiement. Endpoint public sécurisé par vérification " +
            "de signature, pas par JWT/clé API.")
    @PostMapping
    public ResponseEntity<Void> handleWebhook(HttpServletRequest request,
                                              @RequestHeader(value = "X-Signature", required = false) String signature)
            throws IOException {
        String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        purchaseService.handleIncomingWebhook(rawBody, signature);   // ✅ renommé
        return ResponseEntity.ok().build();
    }
}