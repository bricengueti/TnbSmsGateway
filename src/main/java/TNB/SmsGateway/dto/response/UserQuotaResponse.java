package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Vue d'ensemble du quota d'un utilisateur pour le monitoring admin")
public record UserQuotaResponse(
        String userId, String userEmail, String type, String planName, boolean unlimited,

        @Schema(description = "SMS consommés depuis le début de la période (type=POOL uniquement)")
        Integer smsSentInPeriod,

        @Schema(description = "Quota total de SMS pour toute la période (type=POOL uniquement)")
        Integer smsQuota,

        @Schema(description = "Plafond de devices simultanés (type=PERSONAL uniquement)")
        Integer quantityDevices,

        @Schema(description = "Nombre de devices actuellement connectés (type=PERSONAL uniquement)")
        Long currentDeviceCount,

        @Schema(description = "Date de fin de validité du pack — null si aucun pack assigné")
        Instant periodEndsAt,

        @Schema(description = "true si periodEndsAt est dépassée (pack à renouveler)")
        boolean expired
) {}
