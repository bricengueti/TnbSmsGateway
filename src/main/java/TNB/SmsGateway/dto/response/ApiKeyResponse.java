package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Réponse de création de clé API")
public record ApiKeyResponse(
        @Schema(description = "ID de la clé API", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Clé API complète (AFFICHÉE UNE SEULE FOIS)",
                example = "tnb_live_7f2k9xq1m3p5v8d2c4a6")
        String apiKey,

        @Schema(description = "Préfixe de la clé (visible dans le dashboard)",
                example = "tnb_live_7f2k9x...")
        String prefix,

        @Schema(description = "Scope de la clé", example = "SEND_ONLY")
        String scope,

        @Schema(description = "Label descriptif", example = "Production Mobile App")
        String label,

        @Schema(description = "Date de création", example = "2026-07-16T14:30:00Z")
        Instant createdAt
) {}