package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.QuotaOverrideRequest;
import TNB.SmsGateway.dto.response.ManagedApiKeyResponse;
import TNB.SmsGateway.entity.ApiKey;
import TNB.SmsGateway.entity.ApiKeyQuota;
import TNB.SmsGateway.entity.Plan;
import TNB.SmsGateway.entity.RoutingMode;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.ApiKeyQuotaRepository;
import TNB.SmsGateway.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyQuotaRepository apiKeyQuotaRepository;
    private final PlanService planService;

    public AdminApiKeyService(ApiKeyRepository apiKeyRepository,
                              ApiKeyQuotaRepository apiKeyQuotaRepository,
                              PlanService planService) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyQuotaRepository = apiKeyQuotaRepository;
        this.planService = planService;
    }

    public List<ManagedApiKeyResponse> listManagedApiKeys() {
        return apiKeyRepository.findAll().stream()
                .filter(k -> k.getRoutingMode() == RoutingMode.MANAGED_POOL)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ManagedApiKeyResponse assignPlan(UUID apiKeyId, UUID planId) {
        ApiKey apiKey = findManagedKeyOrThrow(apiKeyId);
        Plan plan = planService.findByIdOrThrow(planId);

        ApiKeyQuota quota = apiKeyQuotaRepository.findByApiKeyId(apiKeyId)
                .orElseGet(() -> new ApiKeyQuota(apiKey, false, null));

        quota.setPlan(plan);
        quota.setUnlimited(plan.isUnlimited());
        quota.setMonthlyLimit(plan.getMonthlySmsLimit());
        apiKeyQuotaRepository.save(quota);

        return toResponse(apiKey);
    }

    @Transactional
    public ManagedApiKeyResponse overrideQuota(UUID apiKeyId, QuotaOverrideRequest request) {
        ApiKey apiKey = findManagedKeyOrThrow(apiKeyId);

        ApiKeyQuota quota = apiKeyQuotaRepository.findByApiKeyId(apiKeyId)
                .orElseGet(() -> new ApiKeyQuota(apiKey, false, null));

        if (request.unlimited() != null) quota.setUnlimited(request.unlimited());
        if (request.monthlyLimit() != null) quota.setMonthlyLimit(request.monthlyLimit());
        if (Boolean.TRUE.equals(request.resetCounter())) quota.setSmsSentThisMonth(0);

        apiKeyQuotaRepository.save(quota);
        return toResponse(apiKey);
    }

    private ApiKey findManagedKeyOrThrow(UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new BusinessException("Clé API non trouvée", "API_KEY_NOT_FOUND", 404));
        if (apiKey.getRoutingMode() != RoutingMode.MANAGED_POOL) {
            throw new BusinessException("Cette clé n'est pas en mode pool partagé (routingMode=OWN_DEVICES)",
                    "NOT_A_MANAGED_KEY", 400);
        }
        return apiKey;
    }

    private ManagedApiKeyResponse toResponse(ApiKey apiKey) {
        ApiKeyQuota quota = apiKeyQuotaRepository.findByApiKeyId(apiKey.getId()).orElse(null);

        Integer sent = quota != null ? quota.getSmsSentThisMonth() : null;
        Integer limit = quota != null ? quota.getMonthlyLimit() : null;
        boolean unlimited = quota != null && quota.isUnlimited();
        Integer usagePercent = (!unlimited && limit != null && limit > 0 && sent != null)
                ? Math.min(100, (int) ((sent * 100.0) / limit))
                : null;

        return new ManagedApiKeyResponse(
                apiKey.getId().toString(),
                apiKey.getLabel(),
                apiKey.getUser().getEmail(),
                quota != null && quota.getPlan() != null ? quota.getPlan().getName() : null,
                unlimited,
                limit,
                sent,
                usagePercent,
                quota != null ? quota.getResetAt() : null
        );
    }
}