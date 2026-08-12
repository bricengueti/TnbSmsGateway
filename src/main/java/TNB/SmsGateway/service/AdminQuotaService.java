package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.QuotaOverrideRequest;
import TNB.SmsGateway.dto.response.UserQuotaResponse;
import TNB.SmsGateway.entity.*;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.repository.UserQuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminQuotaService {

    private final UserQuotaRepository userQuotaRepository;
    private final UserService userService;
    private final PlanService planService;
    private final DeviceRepository deviceRepository;

    public AdminQuotaService(UserQuotaRepository userQuotaRepository, UserService userService,
                             PlanService planService, DeviceRepository deviceRepository) {
        this.userQuotaRepository = userQuotaRepository;
        this.userService = userService;
        this.planService = planService;
        this.deviceRepository = deviceRepository;
    }

    public List<UserQuotaResponse> listQuotas(PlanType type) {
        return userQuotaRepository.findAll().stream()
                .filter(q -> q.getType() == type)
                .map(this::toResponse)
                .toList();
    }

    /**
     * SCÉNARIO: Assigner un pack POOL — REMPLACE le quota existant et démarre
     * une nouvelle période (validityMonths du pack), compteur consommé à zéro.
     */
    @Transactional
    public UserQuotaResponse assignPoolPlan(UUID userId, UUID planId) {
        User user = userService.findByIdOrThrow(userId);
        Plan plan = planService.findByIdOrThrow(planId);

        if (plan.getType() != PlanType.POOL) {
            throw new BusinessException("Ce pack n'est pas de type POOL", "PLAN_TYPE_MISMATCH", 400);
        }

        UserQuota quota = getOrCreate(user, PlanType.POOL);
        quota.setPlan(plan);
        quota.setUnlimited(plan.getQuantitySms() == null);
        quota.setSmsQuota(plan.getQuantitySms());
        quota.startNewPeriod(plan); // ✅ remet le compteur à zéro ET recalcule periodEndsAt

        userQuotaRepository.save(quota);
        return toResponse(quota);
    }

    /**
     * SCÉNARIO: Assigner un pack PERSONAL (plafond de devices) — REMPLACE le
     * plafond et démarre une nouvelle période de validité.
     */
    @Transactional
    public UserQuotaResponse assignPersonalPlan(UUID userId, UUID planId) {
        User user = userService.findByIdOrThrow(userId);
        Plan plan = planService.findByIdOrThrow(planId);

        if (plan.getType() != PlanType.PERSONAL) {
            throw new BusinessException("Ce pack n'est pas de type PERSONAL", "PLAN_TYPE_MISMATCH", 400);
        }

        UserQuota quota = getOrCreate(user, PlanType.PERSONAL);
        quota.setPlan(plan);
        quota.setUnlimited(plan.getQuantityDevices() == null);
        quota.setQuantityDevices(plan.getQuantityDevices());
        quota.startNewPeriod(plan); // recalcule periodEndsAt (le plafond devices n'a pas de compteur consommé)

        userQuotaRepository.save(quota);
        return toResponse(quota);
    }

    @Transactional
    public UserQuotaResponse overrideQuota(UUID userId, PlanType type, QuotaOverrideRequest request) {
        User user = userService.findByIdOrThrow(userId);
        UserQuota quota = getOrCreate(user, type);

        if (request.unlimited() != null) quota.setUnlimited(request.unlimited());
        if (type == PlanType.POOL && request.smsQuota() != null) quota.setSmsQuota(request.smsQuota());
        if (type == PlanType.PERSONAL && request.quantityDevices() != null) quota.setQuantityDevices(request.quantityDevices());
        if (Boolean.TRUE.equals(request.resetCounter())) quota.setSmsSentInPeriod(0);
        if (request.periodEndsAt() != null) quota.setPeriodEndsAt(request.periodEndsAt());

        userQuotaRepository.save(quota);
        return toResponse(quota);
    }

    /** Réutilisée par MeController (auto-service) — voir getUserQuotaOrNull. */
    public UserQuotaResponse getUserQuotaOrNull(UUID userId, PlanType type) {
        return userQuotaRepository.findByUserIdAndType(userId, type)
                .map(this::toResponse)
                .orElse(null);
    }

    private UserQuota getOrCreate(User user, PlanType type) {
        return userQuotaRepository.findByUserIdAndType(user.getId(), type)
                .orElseGet(() -> new UserQuota(user, type));
    }

    private UserQuotaResponse toResponse(UserQuota quota) {
        Long deviceCount = quota.getType() == PlanType.PERSONAL
                ? deviceRepository.countPersonalDevicesByUserId(quota.getUser().getId())
                : null;

        return new UserQuotaResponse(
                quota.getUser().getId().toString(),
                quota.getUser().getEmail(),
                quota.getType().name(),
                quota.getPlan() != null ? quota.getPlan().getName() : null,
                quota.isUnlimited(),
                quota.getType() == PlanType.POOL ? quota.getSmsSentInPeriod() : null,
                quota.getType() == PlanType.POOL ? quota.getSmsQuota() : null,
                quota.getType() == PlanType.PERSONAL ? quota.getQuantityDevices() : null,
                deviceCount,
                quota.getPeriodEndsAt(),
                quota.isExpired()
        );
    }
}
