package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Vue d'ensemble d'un utilisateur, pour la liste admin")
public record AdminUserResponse(
        String id,
        String email,
        String companyName,

        @Schema(allowableValues = {"ACTIVE", "SUSPENDED"})
        String status,

        boolean isAdmin,
        long deviceCount,
        long apiKeyCount,
        Instant createdAt
) {}