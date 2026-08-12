package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Ajustement manuel d'un quota — remplace les valeurs existantes")
public record QuotaOverrideRequest(
        @Schema(description = "Rendre le quota illimité", example = "false")
        Boolean unlimited,

        @Schema(description = "Nouveau quota SMS total pour la période (type=POOL)", example = "500")
        Integer smsQuota,

        @Schema(description = "Nouveau plafond de devices (type=PERSONAL)", example = "3")
        Integer quantityDevices,

        @Schema(description = "Remettre le compteur consommé à zéro SANS changer la date d'expiration", example = "false")
        Boolean resetCounter,

        @Schema(description = "Nouvelle date d'expiration — geste commercial pour prolonger un pack " +
                "sans changer le solde consommé. Laisser vide pour ne pas modifier l'expiration.")
        Instant periodEndsAt
) {}
