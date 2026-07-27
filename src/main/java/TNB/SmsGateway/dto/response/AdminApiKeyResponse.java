package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Vue d'ensemble d'une clé API, pour la liste admin")
public record AdminApiKeyResponse(
        String id,
        String ownerEmail,
        String label,
        String keyPrefix,

        @Schema(allowableValues = {"FULL", "SEND_ONLY", "READ_ONLY"})
        String scope,

        @Schema(allowableValues = {"OWN_DEVICES", "MANAGED_POOL"})
        String routingMode,

        boolean revoked,
        Instant createdAt
) {}