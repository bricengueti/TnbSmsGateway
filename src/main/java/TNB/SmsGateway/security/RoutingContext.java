package TNB.SmsGateway.security;


import TNB.SmsGateway.entity.RoutingMode;
import java.util.UUID;

public record RoutingContext(UUID userId, UUID apiKeyId, RoutingMode routingMode) {}