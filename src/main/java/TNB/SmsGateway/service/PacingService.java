package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.DevicePacingRequest;
import TNB.SmsGateway.dto.request.SimPacingRequest;
import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceSim;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.repository.DeviceSimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PacingService {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;
    private final DeviceSimRepository deviceSimRepository;

    public PacingService(DeviceService deviceService,
                         DeviceRepository deviceRepository,
                         DeviceSimRepository deviceSimRepository) {
        this.deviceService = deviceService;
        this.deviceRepository = deviceRepository;
        this.deviceSimRepository = deviceSimRepository;
    }

    @Transactional
    public void updateDevicePacing(UUID userId, UUID deviceId, DevicePacingRequest request) {
        Device device = deviceService.findByIdAndUser(deviceId, userId);
        validateRange(request.minDelaySec(), request.maxDelaySec());
        deviceRepository.updatePacing(device.getId(), request.minDelaySec(), request.maxDelaySec());
    }

    @Transactional
    public void updateSimPacing(UUID userId, UUID deviceId, UUID simId, SimPacingRequest request) {
        Device device = deviceService.findByIdAndUser(deviceId, userId);

        DeviceSim sim = device.getSims().stream()
                .filter(s -> s.getId().equals(simId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("SIM non trouvée pour ce device", "SIM_NOT_FOUND", 404));

        if ((request.minDelaySec() == null) != (request.maxDelaySec() == null)) {
            throw new BusinessException(
                    "minDelaySec et maxDelaySec doivent être fournis ensemble, ou tous les deux null",
                    "INVALID_PACING_OVERRIDE", 400
            );
        }

        if (request.minDelaySec() != null) {
            validateRange(request.minDelaySec(), request.maxDelaySec());
        }

        deviceSimRepository.updatePacing(sim.getId(), request.minDelaySec(), request.maxDelaySec());
    }

    private void validateRange(int minSec, int maxSec) {
        if (minSec > maxSec) {
            throw new BusinessException(
                    "Le délai minimum ne peut pas dépasser le délai maximum",
                    "INVALID_PACING_RANGE", 400
            );
        }
    }
}