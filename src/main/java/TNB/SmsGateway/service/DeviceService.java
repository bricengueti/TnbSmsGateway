package TNB.SmsGateway.service;


import TNB.SmsGateway.dto.request.DeviceRegisterRequest;
import TNB.SmsGateway.dto.request.DeviceUpdateRequest;
import TNB.SmsGateway.dto.response.DeviceResponse;
import TNB.SmsGateway.dto.response.DeviceSimResponse;
import TNB.SmsGateway.entity.Country;
import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.utils.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SERVICE: DeviceService
 *
 * DESCRIPTION: Gère le cycle de vie des devices Android
 * - Enregistrement des devices (génération pairingCode)
 * - Consultation des devices et SIMs
 * - Mise à jour (label, pays)
 * - Suppression logique
 * - Gestion du status (ONLINE/OFFLINE/BUSY/DISABLED)
 *
 * SCÉNARIOS:
 * 1. Enregistrement: l'utilisateur crée un device → code de pairing
 * 2. Consultation: liste des devices avec leurs SIMs
 * 3. Mise à jour: modification du label ou du pays
 * 4. Suppression: retrait d'un device (soft delete)
 * 5. Statut: mise à jour du status (WebSocket)
 */
@Service
public class DeviceService {

    private static final int PAIRING_CODE_EXPIRY_MINUTES = 15;

    private final DeviceRepository deviceRepository;
    private final UserService userService;
    private final ReferenceService referenceService;

    public DeviceService(DeviceRepository deviceRepository,
                         UserService userService,
                         ReferenceService referenceService) {
        this.deviceRepository = deviceRepository;
        this.userService = userService;
        this.referenceService = referenceService;
    }

    /**
     * SCÉNARIO: Enregistrer un nouveau device
     * ÉTAPES:
     * 1. Vérifier que l'utilisateur existe
     * 2. Vérifier que le pays existe
     * 3. Créer le device avec status DISABLED
     * 4. Générer un code de pairing à 6 chiffres
     * 5. Définir l'expiration à 15 minutes
     * 6. Retourner le device avec le code de pairing
     *
     * @param userId ID de l'utilisateur
     * @param request Label et code pays
     * @return DeviceResponse avec code de pairing
     */
    @Transactional
    public DeviceResponse registerDevice(UUID userId, DeviceRegisterRequest request) {
        User user = userService.findByIdOrThrow(userId);

        Country country = referenceService.findCountryByCode(request.countryCode())
                .orElseThrow(() -> new BusinessException("Pays non trouvé", "COUNTRY_NOT_FOUND", 404));

        Device device = new Device();
        device.setUser(user);
        device.setCountry(country);
        device.setLabel(request.label());
        device.setStatus(DeviceStatus.DISABLED);

        String pairingCode = RandomUtils.generatePairingCode();
        device.setPairingCode(pairingCode);
        device.setPairingCodeExpiresAt(Instant.now().plusSeconds(PAIRING_CODE_EXPIRY_MINUTES * 60));

        deviceRepository.save(device);

        return new DeviceResponse(
                device.getId().toString(),
                device.getLabel(),
                device.getCountry().getCode(),
                device.getCountry().getName(),
                device.getStatus().name(),
                device.getPairedAt(),
                device.getLastHeartbeatAt(),
                List.of()
        );
    }

    /**
     * SCÉNARIO: Lister tous les devices d'un utilisateur
     *
     * @param userId ID de l'utilisateur
     * @return Liste des devices avec leurs SIMs
     */
    public List<DeviceResponse> listDevices(UUID userId) {
        List<Device> devices = deviceRepository.findByUserIdWithSims(userId);
        return devices.stream()
                .map(this::toDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * SCÉNARIO: Obtenir les détails d'un device
     *
     * @param userId ID de l'utilisateur
     * @param deviceId ID du device
     * @return DeviceResponse avec les SIMs
     */
    public DeviceResponse getDevice(UUID userId, UUID deviceId) {
        Device device = findByIdAndUser(deviceId, userId);
        return toDeviceResponse(device);
    }

    /**
     * SCÉNARIO: Mettre à jour un device
     *
     * @param userId ID de l'utilisateur
     * @param deviceId ID du device
     * @param request Label ou pays à modifier
     * @return DeviceResponse mis à jour
     */
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
     * SCÉNARIO: Supprimer un device (soft delete)
     *
     * @param userId ID de l'utilisateur
     * @param deviceId ID du device
     */
    @Transactional
    public void deleteDevice(UUID userId, UUID deviceId) {
        Device device = findByIdAndUser(deviceId, userId);
        device.markDeleted(userId);
        deviceRepository.save(device);
    }

    /**
     * SCÉNARIO: Trouver un device par ID et propriétaire
     *
     * @param deviceId ID du device
     * @param userId ID de l'utilisateur
     * @return Device trouvé
     * @throws BusinessException Si device non trouvé
     */
    public Device findByIdAndUser(UUID deviceId, UUID userId) {
        return deviceRepository.findById(deviceId)
                .filter(d -> d.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException("Device non trouvé", "DEVICE_NOT_FOUND", 404));
    }

    /**
     * SCÉNARIO: Trouver un device par ID (sans vérification utilisateur)
     * Utilisé par le WebSocket
     *
     * @param deviceId ID du device
     * @return Device trouvé
     */
    public Device findById(UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("Device non trouvé", "DEVICE_NOT_FOUND", 404));
    }

    /**
     * SCÉNARIO: Mettre à jour le status d'un device (WebSocket)
     *
     * @param deviceId ID du device
     * @param status Nouveau status
     */
    @Transactional
    public void updateDeviceStatus(UUID deviceId, DeviceStatus status) {
        deviceRepository.updateStatus(deviceId, status);
    }

    /**
     * SCÉNARIO: Mettre à jour le heartbeat (WebSocket)
     *
     * @param deviceId ID du device
     */
    @Transactional
    public void updateHeartbeat(UUID deviceId) {
        deviceRepository.updateHeartbeat(deviceId, Instant.now());
    }

    /**
     * Convertir un Device en DeviceResponse
     */
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