package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Vue d'ensemble d'un device, pour la liste admin")
public record AdminDeviceResponse(
        String id,
        String ownerEmail,

        @Schema(allowableValues = {"PERSONAL", "POOL"})
        String type,

        @Schema(allowableValues = {"ONLINE", "OFFLINE", "BUSY", "DISABLED"})
        String status,

        String countryCode,
        String label,
        boolean revoked,
        Instant pairedAt,
        Instant lastHeartbeatAt
) {}