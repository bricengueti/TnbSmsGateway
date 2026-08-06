package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statistiques globales de la plateforme")
public record AdminStatsResponse(
        long totalUsers,
        long activeUsers,
        long suspendedUsers,
        long adminUsers,
        long totalDevices,
        long personalDevices,
        long poolDevices,
        long onlineDevices,
        long revokedDevices,
        long totalApiKeys,
        long ownDevicesKeys,
        long managedPoolKeys,
        long revokedApiKeys,
        long totalMessagesSent,
        long totalPlans
) {}