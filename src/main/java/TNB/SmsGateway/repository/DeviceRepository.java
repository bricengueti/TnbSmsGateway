package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    // ===== FIND =====

    List<Device> findByUserId(UUID userId);

    List<Device> findByUserIdAndStatus(UUID userId, DeviceStatus status);

    List<Device> findByStatus(DeviceStatus status);

    // ❌ Supprimé : findByPairingCode(String pairingCode)
    // Le pairing ne se fait plus via un champ sur Device — voir PairingCodeRepository.

    Optional<Device> findBySecretTokenHash(String secretTokenHash);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.sims WHERE d.id = :deviceId")
    Optional<Device> findByIdWithSims(@Param("deviceId") UUID deviceId);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.sims WHERE d.user.id = :userId")
    List<Device> findByUserIdWithSims(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT d FROM Device d " +
            "JOIN d.sims s " +
            "WHERE d.user.id = :userId " +
            "AND d.country.code = :countryCode " +
            "AND s.operator.code = :operatorCode " +
            "AND d.status = 'ONLINE' " +
            "AND s.isActive = true " +
            "AND d.revokedAt IS NULL")
    List<Device> findCandidateDevicesForQuota(@Param("userId") UUID userId,
                                              @Param("countryCode") String countryCode,
                                              @Param("operatorCode") String operatorCode);

    default List<Device> findAvailableDevices(UUID userId, String countryCode, String operatorCode) {
        return findCandidateDevicesForQuota(userId, countryCode, operatorCode).stream()
                .filter(device -> device.getSims().stream()
                        .anyMatch(sim -> operatorCode.equals(sim.getOperator().getCode())
                                && Boolean.TRUE.equals(sim.getIsActive())
                                && sim.hasQuota()))
                .sorted(Comparator.comparingInt(device -> device.getSims().stream()
                        .filter(sim -> operatorCode.equals(sim.getOperator().getCode()))
                        .mapToInt(sim -> sim.getDailySmsSent() != null ? sim.getDailySmsSent() : 0)
                        .min()
                        .orElse(0)))
                .toList();
    }

    default Optional<Device> findLeastUsedDevice(UUID userId, String countryCode, String operatorCode) {
        return findAvailableDevices(userId, countryCode, operatorCode).stream().findFirst();
    }

    // ===== PAIRING =====
    // ❌ Supprimés : updatePairingCode(...) et markAsPaired(...)
    // Le pairing crée désormais directement une nouvelle ligne Device
    // (via deviceRepository.save(new Device(...))) plutôt que de mettre
    // à jour une ligne existante créée au préalable côté dashboard.

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.status = :status WHERE d.id = :deviceId")
    void updateStatus(@Param("deviceId") UUID deviceId, @Param("status") DeviceStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.lastHeartbeatAt = :heartbeatAt WHERE d.id = :deviceId")
    void updateHeartbeat(@Param("deviceId") UUID deviceId, @Param("heartbeatAt") Instant heartbeatAt);

    // ✅ Ajouté : révocation individuelle d'un device (bouton "Révoquer"
    // dans la liste des devices du dashboard), sans toucher au PairingCode
    // du compte qui reste valable pour les autres devices.
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.revokedAt = :revokedAt, d.status = 'DISABLED' WHERE d.id = :deviceId")
    void revokeById(@Param("deviceId") UUID deviceId, @Param("revokedAt") Instant revokedAt);

    // ===== COVERAGE =====

    @Query("SELECT d.country.code, s.operator.code, COUNT(d) " +
            "FROM Device d " +
            "JOIN d.sims s " +
            "WHERE d.user.id = :userId " +
            "AND d.status = 'ONLINE' " +
            "AND s.isActive = true " +
            "AND d.revokedAt IS NULL " +
            "GROUP BY d.country.code, s.operator.code")
    List<Object[]> getCoverageByUser(@Param("userId") UUID userId);

    @Query("SELECT d.country.code, COUNT(d) " +
            "FROM Device d " +
            "WHERE d.user.id = :userId " +
            "AND d.status = 'ONLINE' " +
            "AND d.revokedAt IS NULL " +
            "GROUP BY d.country.code")
    List<Object[]> getDeviceCountByCountry(@Param("userId") UUID userId);

    // ===== MAINTENANCE =====

    @Query("SELECT d FROM Device d WHERE d.status = 'OFFLINE' AND d.lastHeartbeatAt < :threshold")
    List<Device> findStaleDevices(@Param("threshold") Instant threshold);

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.status = 'OFFLINE' WHERE d.id = :deviceId")
    void markAsOffline(@Param("deviceId") UUID deviceId);
}