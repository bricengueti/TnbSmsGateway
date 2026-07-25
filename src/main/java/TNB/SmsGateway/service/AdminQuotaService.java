package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.QuotaOverrideRequest;
import TNB.SmsGateway.dto.request.TopUpPoolRequest;
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
     * SCÉNARIO: Recharger le solde de crédits POOL (modèle prépayé — ADDITIF, jamais un reset)
     */
    @Transactional
    public UserQuotaResponse topUpPool(UUID userId, TopUpPoolRequest request) {
        User user = userService.findByIdOrThrow(userId);
        UserQuota quota = getOrCreate(user, PlanType.POOL);

        if (request.planId() != null) {
            Plan plan = planService.findByIdOrThrow(UUID.fromString(request.planId()));
            if (plan.getType() != PlanType.POOL) {
                throw new BusinessException("Ce pack n'est pas de type POOL", "PLAN_TYPE_MISMATCH", 400);
            }
            quota.setPlan(plan);
            if (plan.isUnlimitedCredits()) {
                quota.setUnlimited(true);
            } else {
                quota.addCredits(plan.getSmsCredits());
            }
        } else if (request.credits() != null) {
            quota.addCredits(request.credits());
        } else {
            throw new BusinessException("Fournir soit planId, soit credits", "MISSING_TOPUP_AMOUNT", 400);
        }

        userQuotaRepository.save(quota);
        return toResponse(quota);
    }

    /**
     * SCÉNARIO: Assigner un pack PERSONAL (plafond de devices) — REMPLACE la limite, n'additionne pas
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
        quota.setUnlimited(plan.isUnlimitedDevices());
        quota.setMaxDevices(plan.getMaxDevices());

        userQuotaRepository.save(quota);
        return toResponse(quota);
    }

    @Transactional
    public UserQuotaResponse overrideQuota(UUID userId, PlanType type, QuotaOverrideRequest request) {
        User user = userService.findByIdOrThrow(userId);
        UserQuota quota = getOrCreate(user, type);

        if (request.unlimited() != null) quota.setUnlimited(request.unlimited());
        if (type == PlanType.POOL && request.credits() != null) quota.setSmsCreditsRemaining(request.credits());
        if (type == PlanType.PERSONAL && request.maxDevices() != null) quota.setMaxDevices(request.maxDevices());

        userQuotaRepository.save(quota);
        return toResponse(quota);
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
                quota.getType() == PlanType.POOL ? quota.getSmsCreditsRemaining() : null,
                quota.getType() == PlanType.PERSONAL ? quota.getMaxDevices() : null,
                deviceCount
        );
    }
}