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

    public List<DeviceSim> getSimsByDevice(Device device) {
        return deviceSimRepository.findByDevice(device);
    }

    /**
     * Mettre à jour une SIM.
     * quota accepte soit un nombre en chaîne ("100"), soit "ILLIMITE" (insensible à la casse).
     */
    @Transactional
    public DeviceSim updateSim(UUID userId, UUID simId, String operatorCode, Boolean isActive, String quota) {
        DeviceSim sim = deviceSimRepository.findById(simId)
                .orElseThrow(() -> new BusinessException("SIM non trouvée", "SIM_NOT_FOUND", 404));

        Device device = sim.getDevice();
        if (!device.getUser().getId().equals(userId)) {
            throw new BusinessException("Accès non autorisé", "FORBIDDEN", 403);
        }

        if (operatorCode != null && !operatorCode.isEmpty()) {
            Operator operator = referenceService.findOperatorByCode(operatorCode)
                    .orElseThrow(() -> new BusinessException("Opérateur non trouvé", "OPERATOR_NOT_FOUND", 404));

            if (!operator.getCountry().getCode().equals(device.getCountry().getCode())) {
                throw new BusinessException("L'opérateur n'appartient pas au pays du device",
                        "OPERATOR_COUNTRY_MISMATCH", 400);
            }
            sim.setOperator(operator);
        }

        if (isActive != null) {
            sim.setIsActive(isActive);
        }

        if (quota != null && !quota.isBlank()) {
            if (DeviceSim.QUOTA_UNLIMITED.equalsIgnoreCase(quota)) {
                sim.setDailySmsQuota(DeviceSim.QUOTA_UNLIMITED);
            } else {
                try {
                    int parsed = Integer.parseInt(quota.trim());
                    if (parsed <= 0) {
                        throw new BusinessException("Le quota doit être positif", "INVALID_QUOTA", 400);
                    }
                    sim.setDailySmsQuota(String.valueOf(parsed));
                } catch (NumberFormatException e) {
                    throw new BusinessException(
                            "Quota invalide, attendu un nombre ou \"ILLIMITE\"", "INVALID_QUOTA", 400);
                }
            }
        }

        return deviceSimRepository.save(sim);
    }

    @Transactional
    public void resetDailyCounters() {
        deviceSimRepository.resetDailyCounters();
    }
}