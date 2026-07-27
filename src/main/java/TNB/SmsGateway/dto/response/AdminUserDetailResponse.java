package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Détail complet d'un utilisateur pour la vue admin")
public record AdminUserDetailResponse(
        String id,
        String email,
        String companyName,
        String status,
        boolean isAdmin,
        Instant createdAt,
        List<AdminDeviceResponse> devices,
        List<AdminApiKeyResponse> apiKeys,
        UserQuotaResponse poolQuota,
        UserQuotaResponse personalQuota
) {}