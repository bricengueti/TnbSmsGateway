package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.dto.response.PlanResponse;
import TNB.SmsGateway.payment.PaymentInitiationResult;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.PlanService;
import TNB.SmsGateway.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/purchases")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Achats", description = "Achat de packs (crédits SMS ou plafond devices)")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PlanService planService;
    public PurchaseController(PurchaseService purchaseService, PlanService planService) {
        this.purchaseService = purchaseService;
        this.planService = planService;
    }

    @Operation(summary = "Acheter un pack", description = "Initie le paiement pour le pack choisi. " +
            "Retourne une URL de paiement à ouvrir. Le pack est appliqué automatiquement une fois le " +
            "paiement confirmé via webhook — pas besoin de rappeler quoi que ce soit après paiement.")
    @PostMapping("/{planId}")
    public ResponseEntity<PaymentInitiationResult> purchasePlan(
            @PathVariable UUID planId,
            Authentication authentication
    ) {
        UUID userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(purchaseService.initiatePurchase(userId, planId));
    }

    @Operation(summary = "Lister tous les packs")
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> listPlans() {
        return ResponseEntity.ok(planService.listAllPlans());
    }

}