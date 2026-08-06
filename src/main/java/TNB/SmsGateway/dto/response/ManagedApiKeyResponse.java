package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Vue d'ensemble d'une clé API en mode pool partagé, pour le monitoring admin")
public record ManagedApiKeyResponse(
        @Schema(description = "ID de la clé API") String apiKeyId,
        @Schema(description = "Label de la clé") String label,
        @Schema(description = "Email du compte propriétaire") String ownerEmail,
        @Schema(description = "Nom du pack assigné, null si quota configuré manuellement") String planName,
        @Schema(description = "true si illimité") boolean unlimited,
        @Schema(description = "Limite mensuelle, null si illimité") Integer monthlyLimit,
        @Schema(description = "SMS déjà envoyés ce mois-ci") Integer smsSentThisMonth,
        @Schema(description = "Pourcentage du quota consommé (0-100), null si illimité") Integer usagePercent,
        @Schema(description = "Date de prochaine réinitialisation du compteur") Instant resetAt
) {}