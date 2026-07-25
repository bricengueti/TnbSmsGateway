package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ajustement manuel d'un quota (geste commercial, remboursement, correction) — " +
        "remplace les valeurs existantes plutôt que de les additionner")
public record QuotaOverrideRequest(
        @Schema(description = "Rendre le quota illimité", example = "false")
        Boolean unlimited,

        @Schema(description = "Nouveau solde de crédits SMS exact (type=POOL)", example = "500")
        Integer credits,

        @Schema(description = "Nouveau plafond de devices exact (type=PERSONAL)", example = "3")
        Integer maxDevices
) {}