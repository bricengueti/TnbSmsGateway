package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceSim;
import TNB.SmsGateway.entity.Operator;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.DeviceSimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceSimService {

    private final DeviceSimRepository deviceSimRepository;
    private final DeviceService deviceService;
    private final ReferenceService referenceService;

    public DeviceSimService(DeviceSimRepository deviceSimRepository,
                            DeviceService deviceService,
                            ReferenceService referenceService) {
        this.deviceSimRepository = deviceSimRepository;
        this.deviceService = deviceService;
        this.referenceService = referenceService;
    }

    /**
     * Récupérer toutes les SIMs d'un device
     */
    public List<DeviceSim> getSimsByDevice(Device device) {
        return deviceSimRepository.findByDevice(device);
    }

    /**
     * Mettre à jour une SIM
     * Scénario: L'utilisateur modifie les paramètres d'une SIM
     */
    @Transactional
    public DeviceSim updateSim(UUID userId, UUID simId, String operatorCode, Boolean isActive, Integer quota) {
        // 1. Trouver la SIM
        DeviceSim sim = deviceSimRepository.findById(simId)
                .orElseThrow(() -> new BusinessException("SIM non trouvée", "SIM_NOT_FOUND", 404));

        // 2. Vérifier que l'utilisateur est propriétaire du device
        Device device = sim.getDevice();
        if (!device.getUser().getId().equals(userId)) {
            throw new BusinessException("Accès non autorisé", "FORBIDDEN", 403);
        }

        // 3. Mettre à jour l'opérateur
        if (operatorCode != null && !operatorCode.isEmpty()) {
            Operator operator = referenceService.findOperatorByCode(operatorCode)
                    .orElseThrow(() -> new BusinessException("Opérateur non trouvé", "OPERATOR_NOT_FOUND", 404));

            // Vérifier que l'opérateur appartient au pays du device
            if (!operator.getCountry().getCode().equals(device.getCountry().getCode())) {
                throw new BusinessException("L'opérateur n'appartient pas au pays du device",
                        "OPERATOR_COUNTRY_MISMATCH", 400);
            }
            sim.setOperator(operator);
        }

        // 4. Mettre à jour l'activité
        if (isActive != null) {
            sim.setIsActive(isActive);
        }

        // 5. Mettre à jour le quota
        if (quota != null && quota > 0) {
            sim.setDailySmsQuota(quota);
        }

        return deviceSimRepository.save(sim);
    }

    /**
     * Réinitialiser les quotas quotidiens
     * Scénario: Tâche planifiée à minuit
     */
    @Transactional
    public void resetDailyCounters() {
        deviceSimRepository.resetDailyCounters();
    }
}