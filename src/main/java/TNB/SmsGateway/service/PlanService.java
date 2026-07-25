package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.PlanRequest;
import TNB.SmsGateway.dto.response.PlanResponse;
import TNB.SmsGateway.entity.Plan;
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
        Plan plan = new Plan(request.name(), request.description(),
                request.monthlySmsLimit(), request.priceMonthly());
        return toResponse(planRepository.save(plan));
    }

    public List<PlanResponse> listActivePlans() {
        return planRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    public List<PlanResponse> listAllPlans() {
        return planRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deactivatePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Pack non trouvé", "PLAN_NOT_FOUND", 404));
        plan.setActive(false);
        planRepository.save(plan);
    }

    public Plan findByIdOrThrow(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Pack non trouvé", "PLAN_NOT_FOUND", 404));
    }

    private PlanResponse toResponse(Plan plan) {
        return new PlanResponse(
                plan.getId().toString(),
                plan.getName(),
                plan.getDescription(),
                plan.getMonthlySmsLimit(),
                plan.isUnlimited(),
                plan.getPriceMonthly(),
                plan.isActive()
        );
    }
}