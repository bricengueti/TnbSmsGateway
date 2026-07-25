package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vue d'ensemble du quota d'un utilisateur pour le monitoring admin")
public record UserQuotaResponse(
        String userId,
        String userEmail,

        @Schema(allowableValues = {"POOL", "PERSONAL"})
        String type,

        @Schema(description = "Dernier pack assigné, null si configuré manuellement uniquement")
        String planName,

        boolean unlimited,

        @Schema(description = "Solde de crédits SMS restants (POOL uniquement)")
        Integer smsCreditsRemaining,

        @Schema(description = "Plafond de devices (PERSONAL uniquement)")
        Integer maxDevices,

        @Schema(description = "Nombre de devices personnels actuellement pairés (PERSONAL uniquement, " +
                "à comparer à maxDevices)")
        Long currentDeviceCount
) {}