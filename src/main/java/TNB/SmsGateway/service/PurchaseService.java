package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.*;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.payment.PaymentInitiationResult;
import TNB.SmsGateway.payment.PaymentProvider;
import TNB.SmsGateway.payment.WebhookPaymentEvent;
import TNB.SmsGateway.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PlanService planService;
    private final UserService userService;
    private final PaymentProvider paymentProvider;   // ✅ injecté via l'interface, jamais le mock directement
    private final AdminQuotaService adminQuotaService;

    public PurchaseService(PurchaseOrderRepository purchaseOrderRepository,
                           PlanService planService,
                           UserService userService,
                           PaymentProvider paymentProvider,
                           AdminQuotaService adminQuotaService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.planService = planService;
        this.userService = userService;
        this.paymentProvider = paymentProvider;
        this.adminQuotaService = adminQuotaService;
    }

    /**
     * SCÉNARIO: Le client choisit un pack et initie le paiement.
     */
    @Transactional
    public PaymentInitiationResult initiatePurchase(UUID userId, UUID planId) {
        User user = userService.findByIdOrThrow(userId);
        Plan plan = planService.findByIdOrThrow(planId);

        if (!plan.isActive()) {
            throw new BusinessException("Ce pack n'est plus disponible à la vente", "PLAN_INACTIVE", 400);
        }

        PurchaseOrder order = new PurchaseOrder(user, plan, paymentProvider.getProviderName(), plan.getPrice());

        PaymentInitiationResult result = paymentProvider.initiatePayment(order);
        order.setProviderReference(result.providerReference());
        purchaseOrderRepository.save(order);

        log.info("💳 Achat initié: user={}, plan={}, ref={}", user.getEmail(), plan.getName(), result.providerReference());
        return result;
    }

    /**
     * SCÉNARIO: Un vrai webhook HTTP entrant (provider externe) confirme/infirme un paiement.
     * Vérifie la signature avant tout traitement.
     */
    @Transactional
    public void handleIncomingWebhook(String rawBody, String signatureHeader) {
        if (!paymentProvider.verifyWebhookSignature(rawBody, signatureHeader)) {
            throw new BusinessException("Signature de webhook invalide", "INVALID_WEBHOOK_SIGNATURE", 401);
        }

        WebhookPaymentEvent event = paymentProvider.parseWebhookPayload(rawBody);
        processEvent(event);
    }

    /**
     * SCÉNARIO: Appel interne (mock uniquement) — simule un webhook sans passer par HTTP,
     * donc sans signature à vérifier (l'appel est déjà interne/de confiance).
     * ❌ À supprimer avec MockPaymentProvider/PurchaseWebhookDispatcher quand un vrai
     * provider est branché — plus personne n'appellera cette méthode à ce moment-là.
     */
    @Transactional
    public void simulateWebhookEvent(String rawBody) {
        WebhookPaymentEvent event = paymentProvider.parseWebhookPayload(rawBody);
        processEvent(event);
    }

    private void processEvent(WebhookPaymentEvent event) {
        PurchaseOrder order = purchaseOrderRepository.findByProviderReference(event.providerReference())
                .orElseThrow(() -> new BusinessException("Commande introuvable pour cette référence",
                        "PURCHASE_ORDER_NOT_FOUND", 404));

        if (order.getStatus() != PurchaseStatus.PENDING) {
            log.info("⚠️ Webhook ignoré (déjà traité): ref={}, status actuel={}",
                    event.providerReference(), order.getStatus());
            return;   // ✅ idempotence — évite un double crédit si le webhook arrive 2 fois
        }

        if (!event.success()) {
            order.setStatus(PurchaseStatus.FAILED);
            purchaseOrderRepository.save(order);
            log.warn("❌ Paiement échoué: ref={}", event.providerReference());
            return;
        }

        order.setStatus(PurchaseStatus.PAID);
        purchaseOrderRepository.save(order);

        applyPlanToUser(order);

        log.info("✅ Paiement confirmé et pack appliqué: user={}, plan={}",
                order.getUser().getEmail(), order.getPlan().getName());
    }

    private void applyPlanToUser(PurchaseOrder order) {
        Plan plan = order.getPlan();
        UUID userId = order.getUser().getId();

        // ✅ Appel direct au service métier — PAS de rôle admin nécessaire ici,
        // c'est un appel interne Spring (bean-à-bean), pas une requête HTTP.
        if (plan.getType() == PlanType.POOL) {
            adminQuotaService.topUpPool(userId, new TNB.SmsGateway.dto.request.TopUpPoolRequest(plan.getId().toString(), null));
        } else {
            adminQuotaService.assignPersonalPlan(userId, plan.getId());
        }
    }
}