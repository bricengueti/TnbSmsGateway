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

    Optional<Device> findByPairingCode(String pairingCode);

    Optional<Device> findBySecretTokenHash(String secretTokenHash);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.sims WHERE d.id = :deviceId")
    Optional<Device> findByIdWithSims(@Param("deviceId") UUID deviceId);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.sims WHERE d.user.id = :userId")
    List<Device> findByUserIdWithSims(@Param("userId") UUID userId);

    /**
     * 🔥 CORRIGÉ: retire "s.dailySmsSent < s.dailySmsQuota" du JPQL
     * (impossible de comparer Integer et String directement).
     * Le filtrage sur le quota restant se fait maintenant côté Java,
     * dans findAvailableDevices(...) ci-dessous, via sim.hasQuota().
     */
    @Query("SELECT DISTINCT d FROM Device d " +
            "JOIN d.sims s " +
            "WHERE d.user.id = :userId " +
            "AND d.country.code = :countryCode " +
            "AND s.operator.code = :operatorCode " +
            "AND d.status = 'ONLINE' " +
            "AND s.isActive = true")
    List<Device> findCandidateDevicesForQuota(@Param("userId") UUID userId,
                                              @Param("countryCode") String countryCode,
                                              @Param("operatorCode") String operatorCode);

    /**
     * 🔥 NOUVEAU: remplace l'ancienne findAvailableDevices() basée sur JPQL cassé.
     * Filtre en Java les devices dont AU MOINS UNE sim correspondante a encore du quota.
     * Résultat trié par dailySmsSent croissant de la sim concernée (comportement équivalent
     * à l'ancien ORDER BY s.dailySmsSent ASC).
     */
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

    /**
     * 🔥 NOUVEAU: remplace l'ancienne findLeastUsedDevice() basée sur JPQL cassé.
     * Réutilise findAvailableDevices() (déjà trié par usage croissant) et prend le premier.
     */
    default Optional<Device> findLeastUsedDevice(UUID userId, String countryCode, String operatorCode) {
        return findAvailableDevices(userId, countryCode, operatorCode).stream().findFirst();
    }

    // ===== PAIRING =====

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.pairingCode = :pairingCode, d.pairingCodeExpiresAt = :expiresAt WHERE d.id = :deviceId")
    void updatePairingCode(@Param("deviceId") UUID deviceId,
                           @Param("pairingCode") String pairingCode,
                           @Param("expiresAt") Instant expiresAt);

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.pairedAt = :pairedAt, d.secretTokenHash = :secretHash, d.status = 'OFFLINE' " +
            "WHERE d.id = :deviceId")
    void markAsPaired(@Param("deviceId") UUID deviceId,
                      @Param("pairedAt") Instant pairedAt,
                      @Param("secretHash") String secretHash);

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.status = :status WHERE d.id = :deviceId")
    void updateStatus(@Param("deviceId") UUID deviceId, @Param("status") DeviceStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.lastHeartbeatAt = :heartbeatAt WHERE d.id = :deviceId")
    void updateHeartbeat(@Param("deviceId") UUID deviceId, @Param("heartbeatAt") Instant heartbeatAt);

    // ===== COVERAGE =====

    @Query("SELECT d.country.code, s.operator.code, COUNT(d) " +
            "FROM Device d " +
            "JOIN d.sims s " +
            "WHERE d.user.id = :userId " +
            "AND d.status = 'ONLINE' " +
            "AND s.isActive = true " +
            "GROUP BY d.country.code, s.operator.code")
    List<Object[]> getCoverageByUser(@Param("userId") UUID userId);

    @Query("SELECT d.country.code, COUNT(d) " +
            "FROM Device d " +
            "WHERE d.user.id = :userId " +
            "AND d.status = 'ONLINE' " +
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