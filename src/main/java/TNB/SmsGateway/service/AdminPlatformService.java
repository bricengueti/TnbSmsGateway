package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.UpdateUserStatusRequest;
import TNB.SmsGateway.dto.response.*;
import TNB.SmsGateway.entity.*;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminPlatformService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PlanRepository planRepository;
    private final MessageRepository messageRepository;
    private final UserQuotaRepository userQuotaRepository;

    public AdminPlatformService(UserRepository userRepository, DeviceRepository deviceRepository,
                                ApiKeyRepository apiKeyRepository, PlanRepository planRepository,
                                MessageRepository messageRepository, UserQuotaRepository userQuotaRepository) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.planRepository = planRepository;
        this.messageRepository = messageRepository;
        this.userQuotaRepository = userQuotaRepository;
    }

    // ===== STATS GLOBALES =====

    public AdminStatsResponse getGlobalStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                userRepository.countByStatus(UserStatus.SUSPENDED),
                userRepository.countByIsAdminTrue(),
                deviceRepository.count(),
                deviceRepository.countByTypeAndRevokedAtIsNull(DeviceType.PERSONAL),
                deviceRepository.countByTypeAndRevokedAtIsNull(DeviceType.POOL),
                deviceRepository.countByStatusAndRevokedAtIsNull(DeviceStatus.ONLINE),
                deviceRepository.countByRevokedAtIsNotNull(),
                apiKeyRepository.count(),
                apiKeyRepository.countByRoutingMode(RoutingMode.OWN_DEVICES),
                apiKeyRepository.countByRoutingMode(RoutingMode.MANAGED_POOL),
                apiKeyRepository.countByRevokedAtIsNotNull(),
                messageRepository.count(),
                planRepository.count()
        );
    }

    // ===== USERS =====

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toUserSummary).toList();
    }

    public AdminUserDetailResponse getUserDetail(UUID userId) {
        User user = findUserOrThrow(userId);

        List<AdminDeviceResponse> devices = deviceRepository.findByUserId(userId).stream()
                .map(this::toDeviceResponse).toList();

        List<AdminApiKeyResponse> apiKeys = apiKeyRepository.findByUserId(userId).stream()
                .map(this::toApiKeyResponse).toList();

        UserQuotaResponse poolQuota = userQuotaRepository.findByUserIdAndType(userId, PlanType.POOL)
                .map(this::toQuotaResponse).orElse(null);
        UserQuotaResponse personalQuota = userQuotaRepository.findByUserIdAndType(userId, PlanType.PERSONAL)
                .map(this::toQuotaResponse).orElse(null);

        return new AdminUserDetailResponse(
                user.getId().toString(), user.getEmail(), user.getCompanyName(),
                user.getStatus().name(), user.isAdmin(), user.getCreatedAt(),
                devices, apiKeys, poolQuota, personalQuota
        );
    }

    @Transactional
    public AdminUserResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        User user = findUserOrThrow(userId);
        UserStatus status = parseUserStatus(request.status());
        user.setStatus(status);
        userRepository.save(user);
        return toUserSummary(user);
    }

    // ===== DEVICES =====

    public List<AdminDeviceResponse> listDevices(DeviceType type, DeviceStatus status) {
        return deviceRepository.findAll().stream()
                .filter(d -> type == null || d.getType() == type)
                .filter(d -> status == null || d.getStatus() == status)
                .map(this::toDeviceResponse)
                .toList();
    }

    @Transactional
    public void revokeDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("Device non trouvé", "DEVICE_NOT_FOUND", 404));
        device.revoke();
        deviceRepository.save(device);
    }

    @Transactional
    public AdminDeviceResponse setPoolFallback(UUID deviceId, boolean enabled) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("Device non trouvé", "DEVICE_NOT_FOUND", 404));

        if (device.getType() != DeviceType.PERSONAL) {
            throw new BusinessException("Seuls les devices PERSONAL peuvent être activés en secours pool " +
                    "(un device POOL sert déjà le pool nativement)", "NOT_A_PERSONAL_DEVICE", 400);
        }

        device.setAvailableForPool(enabled);
        deviceRepository.save(device);
        return toDeviceResponse(device);
    }

    // ===== API KEYS =====

    public List<AdminApiKeyResponse> listApiKeys(RoutingMode routingMode) {
        return apiKeyRepository.findAll().stream()
                .filter(k -> routingMode == null || k.getRoutingMode() == routingMode)
                .map(this::toApiKeyResponse)
                .toList();
    }

    @Transactional
    public void revokeApiKey(UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new BusinessException("Clé API non trouvée", "API_KEY_NOT_FOUND", 404));
        apiKey.revoke();
        apiKeyRepository.save(apiKey);
    }

    // ===== HELPERS =====

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé", "USER_NOT_FOUND", 404));
    }

    private UserStatus parseUserStatus(String value) {
        try {
            return UserStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Statut invalide: '" + value + "'. Valeurs acceptées: ACTIVE, SUSPENDED",
                    "INVALID_USER_STATUS", 400);
        }
    }

    private AdminUserResponse toUserSummary(User user) {
        return new AdminUserResponse(
                user.getId().toString(), user.getEmail(), user.getCompanyName(),
                user.getStatus().name(), user.isAdmin(),
                deviceRepository.countByUserId(user.getId()),
                apiKeyRepository.countByUserId(user.getId()),
                user.getCreatedAt()
        );
    }

    private AdminDeviceResponse toDeviceResponse(Device device) {
        return new AdminDeviceResponse(
                device.getId().toString(), device.getUser().getEmail(), device.getType().name(),
                device.getStatus().name(), device.getCountry().getCode(), device.getLabel(),
                device.isRevoked(), device.isAvailableForPool(),   // ✅ ajouté
                device.getPairedAt(), device.getLastHeartbeatAt()
        );
    }

    private AdminApiKeyResponse toApiKeyResponse(ApiKey apiKey) {
        return new AdminApiKeyResponse(
                apiKey.getId().toString(),
                apiKey.getUser().getEmail(),
                apiKey.getLabel(),
                apiKey.getKeyPrefix(),
                apiKey.getScope().name(),
                apiKey.getRoutingMode().name(),
                apiKey.isRevoked(),
                apiKey.getCreatedAt()
        );
    }

    private UserQuotaResponse toQuotaResponse(UserQuota quota) {
        return new UserQuotaResponse(
                quota.getUser().getId().toString(), quota.getUser().getEmail(), quota.getType().name(),
                quota.getPlan() != null ? quota.getPlan().getName() : null, quota.isUnlimited(),
                quota.getType() == PlanType.POOL ? quota.getSmsSentInPeriod() : null,
                quota.getType() == PlanType.POOL ? quota.getSmsQuota() : null,
                quota.getType() == PlanType.PERSONAL ? quota.getQuantityDevices() : null,
                quota.getType() == PlanType.PERSONAL ? deviceRepository.countPersonalDevicesByUserId(quota.getUser().getId()) : null,
                quota.getPeriodEndsAt(),
                quota.isExpired()
        );
    }
}