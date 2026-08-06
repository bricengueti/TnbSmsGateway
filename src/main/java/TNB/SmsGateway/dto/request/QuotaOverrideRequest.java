package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ajustement manuel d'un quota — remplace les valeurs existantes")
public record QuotaOverrideRequest(
        @Schema(description = "Rendre le quota illimité", example = "false")
        Boolean unlimited,

        @Schema(description = "Nouvelle limite mensuelle (type=POOL)", example = "500")
        Integer monthlyLimit,

        @Schema(description = "Nouveau plafond de devices (type=PERSONAL)", example = "3")
        Integer maxDevices,

        @Schema(description = "Remettre le compteur du mois en cours à zéro", example = "false")
        Boolean resetCounter
) {}