package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vue d'ensemble du quota d'un utilisateur pour le monitoring admin")
public record UserQuotaResponse(
        String userId, String userEmail, String type, String planName, boolean unlimited,
        Integer smsSentThisMonth, Integer monthlyLimit, Integer maxDevices, Long currentDeviceCount
) {}