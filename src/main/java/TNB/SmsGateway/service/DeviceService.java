package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.DeviceUpdateRequest;
import TNB.SmsGateway.dto.response.DeviceResponse;
import TNB.SmsGateway.dto.response.DeviceSimResponse;
import TNB.SmsGateway.entity.Country;
import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SERVICE: DeviceService
 *
 * DESCRIPTION: Gère le cycle de vie des devices Android déjà pairés
 * - Consultation des devices et SIMs
 * - Mise à jour (label, pays)
 * - Révocation individuelle et suppression logique
 * - Gestion du status (ONLINE/OFFLINE/BUSY/DISABLED)
 *
 * ❌ registerDevice() supprimé : la création d'un Device ne se fait plus
 * manuellement depuis le dashboard, mais automatiquement au moment du
 * pairing (voir DevicePairingService.pairDevice()).
 */
@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final ReferenceService referenceService;

    public DeviceService(DeviceRepository deviceRepository,
                         ReferenceService referenceService) {
        this.deviceRepository = deviceRepository;
        this.referenceService = referenceService;
    }

    public List<DeviceResponse> listDevices(UUID userId) {
        List<Device> devices = deviceRepository.findByUserIdWithSims(userId);
        return devices.stream()
                .map(this::toDeviceResponse)
                .collect(Collectors.toList());
    }

    public DeviceResponse getDevice(UUID userId, UUID deviceId) {
        Device device = findByIdAndUser(deviceId, userId);
        return toDeviceResponse(device);
    }

    @Transactional
    public DeviceResponse updateDevice(UUID userId, UUID deviceId, DeviceUpdateRequest request) {
        Device device = findByIdAndUser(deviceId, userId);

        if (request.label() != null) {
            device.setLabel(request.label());
        }

        if (request.countryCode() != null) {
            Country country = referenceService.findCountryByCode(request.countryCode())
                    .orElseThrow(() -> new BusinessException("Pays non trouvé", "COUNTRY_NOT_FOUND", 404));
            device.setCountry(country);
        }

        deviceRepository.save(device);
        return toDeviceResponse(device);
    }

    /**
     * SCÉNARIO: Révoquer un device individuellement (bouton dans le
     * dashboard), sans affecter le code de connexion du compte ni les
     * autres devices pairés avec ce même code.
     */
    @Transactional
    public void revokeDevice(UUID userId, UUID deviceId) {
        Device device = findByIdAndUser(deviceId, userId);
        deviceRepository.revokeById(device.getId(), Instant.now());
        log.info("🚫 Device {} révoqué par user {}", deviceId, userId);
    }

    @Transactional
    public void deleteDevice(UUID userId, UUID deviceId) {
        Device device = findByIdAndUser(deviceId, userId);
        device.markDeleted(userId);
        deviceRepository.save(device);
    }

    public Device findByIdAndUser(UUID deviceId, UUID userId) {
        return deviceRepository.findById(deviceId)
                .filter(d -> d.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException("Device non trouvé", "DEVICE_NOT_FOUND", 404));
    }

    public Device findById(UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("Device non trouvé", "DEVICE_NOT_FOUND", 404));
    }

    @Transactional
    public void updateDeviceStatus(UUID deviceId, DeviceStatus status) {
        deviceRepository.updateStatus(deviceId, status);
    }

    @Transactional
    public void updateHeartbeat(UUID deviceId) {
        deviceRepository.updateHeartbeat(deviceId, Instant.now());
    }
    private DeviceResponse toDeviceResponse(Device device) {
        List<DeviceSimResponse> sims = device.getSims().stream()
                .map(sim -> new DeviceSimResponse(
                        sim.getId().toString(),
                        sim.getSlotIndex(),
                        sim.getOperator().getCode(),
                        sim.getOperator().getDisplayName(),
                        sim.getPhoneNumber(),
                        sim.getIsActive(),
                        sim.getDailySmsSent(),
                        sim.getDailySmsQuota()
                ))
                .collect(Collectors.toList());

        return new DeviceResponse(
                device.getId().toString(),
                device.getLabel(),
                device.getCountry().getCode(),
                device.getCountry().getName(),
                device.getStatus().name(),
                device.getPairedAt(),
                device.getLastHeartbeatAt(),
                sims
        );
    }
}