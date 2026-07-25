package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.PlanRequest;
import TNB.SmsGateway.dto.response.PlanResponse;
import TNB.SmsGateway.entity.Plan;
import TNB.SmsGateway.entity.PlanType;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        PlanType type = parsePlanType(request.type());

        Plan plan = new Plan(request.name(), request.description(), type,
                request.smsCredits(), request.maxDevices(), null, request.price());
        return toResponse(planRepository.save(plan));
    }

    public List<PlanResponse> listActivePlans(PlanType type) {
        return planRepository.findByActiveTrueAndType(type).stream().map(this::toResponse).toList();
    }

    public List<PlanResponse> listAllPlans() {
        return planRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deactivatePlan(UUID planId) {
        Plan plan = findByIdOrThrow(planId);
        plan.setActive(false);
        planRepository.save(plan);
    }

    public Plan findByIdOrThrow(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Pack non trouvé", "PLAN_NOT_FOUND", 404));
    }

    private PlanType parsePlanType(String value) {
        try {
            return PlanType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Type de pack invalide: '" + value + "'. Valeurs acceptées: POOL, PERSONAL",
                    "INVALID_PLAN_TYPE", 400);
        }
    }

    private PlanResponse toResponse(Plan plan) {
        return new PlanResponse(
                plan.getId().toString(), plan.getName(), plan.getDescription(),
                plan.getType().name(), plan.getSmsCredits(), plan.getMaxDevices(),
                plan.getPrice(), plan.isActive()
        );
    }
}