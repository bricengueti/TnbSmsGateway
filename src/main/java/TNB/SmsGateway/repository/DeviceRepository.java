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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    // ===== FIND =====

    /**
     * Trouver tous les devices d'un utilisateur
     */
    List<Device> findByUserId(UUID userId);

    /**
     * Trouver tous les devices d'un utilisateur par status
     */
    List<Device> findByUserIdAndStatus(UUID userId, DeviceStatus status);

    /**
     * Trouver tous les devices par status
     */
    List<Device> findByStatus(DeviceStatus status);

    /**
     * Trouver un device par son pairing code
     */
    Optional<Device> findByPairingCode(String pairingCode);

    /**
     * Trouver un device par son secret token hash
     */
    Optional<Device> findBySecretTokenHash(String secretTokenHash);

    /**
     * Trouver un device avec ses SIMs
     */
    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.sims WHERE d.id = :deviceId")
    Optional<Device> findByIdWithSims(@Param("deviceId") UUID deviceId);

    /**
     * Trouver les devices d'un utilisateur avec leurs SIMs
     */
    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.sims WHERE d.user.id = :userId")
    List<Device> findByUserIdWithSims(@Param("userId") UUID userId);

    /**
     * Trouver un device online disponible pour un pays et opérateur
     */
    @Query("SELECT d FROM Device d " +
            "JOIN d.sims s " +
            "WHERE d.user.id = :userId " +
            "AND d.country.code = :countryCode " +
            "AND s.operator.code = :operatorCode " +
            "AND d.status = 'ONLINE' " +
            "AND s.isActive = true " +
            "AND s.dailySmsSent < s.dailySmsQuota " +
            "ORDER BY s.dailySmsSent ASC")
    List<Device> findAvailableDevices(@Param("userId") UUID userId,
                                      @Param("countryCode") String countryCode,
                                      @Param("operatorCode") String operatorCode);

    /**
     * Trouver le device le moins utilisé
     */
    @Query("SELECT d FROM Device d " +
            "JOIN d.sims s " +
            "WHERE d.user.id = :userId " +
            "AND d.country.code = :countryCode " +
            "AND s.operator.code = :operatorCode " +
            "AND d.status = 'ONLINE' " +
            "AND s.isActive = true " +
            "AND s.dailySmsSent < s.dailySmsQuota " +
            "ORDER BY s.dailySmsSent ASC LIMIT 1")
    Optional<Device> findLeastUsedDevice(@Param("userId") UUID userId,
                                         @Param("countryCode") String countryCode,
                                         @Param("operatorCode") String operatorCode);

    // ===== PAIRING =====

    /**
     * Mettre à jour le pairing code
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.pairingCode = :pairingCode, d.pairingCodeExpiresAt = :expiresAt WHERE d.id = :deviceId")
    void updatePairingCode(@Param("deviceId") UUID deviceId,
                           @Param("pairingCode") String pairingCode,
                           @Param("expiresAt") Instant expiresAt);

    /**
     * Marquer un device comme pairé
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.pairedAt = :pairedAt, d.secretTokenHash = :secretHash, d.status = 'OFFLINE' " +
            "WHERE d.id = :deviceId")
    void markAsPaired(@Param("deviceId") UUID deviceId,
                      @Param("pairedAt") Instant pairedAt,
                      @Param("secretHash") String secretHash);

    /**
     * Mettre à jour le status
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.status = :status WHERE d.id = :deviceId")
    void updateStatus(@Param("deviceId") UUID deviceId, @Param("status") DeviceStatus status);

    /**
     * Mettre à jour le heartbeat
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.lastHeartbeatAt = :heartbeatAt WHERE d.id = :deviceId")
    void updateHeartbeat(@Param("deviceId") UUID deviceId, @Param("heartbeatAt") Instant heartbeatAt);

    // ===== COVERAGE =====

    /**
     * Compter les devices actifs par pays et opérateur
     */
    @Query("SELECT d.country.code, s.operator.code, COUNT(d) " +
            "FROM Device d " +
            "JOIN d.sims s " +
            "WHERE d.user.id = :userId " +
            "AND d.status = 'ONLINE' " +
            "AND s.isActive = true " +
            "GROUP BY d.country.code, s.operator.code")
    List<Object[]> getCoverageByUser(@Param("userId") UUID userId);

    /**
     * Compter les devices actifs par pays
     */
    @Query("SELECT d.country.code, COUNT(d) " +
            "FROM Device d " +
            "WHERE d.user.id = :userId " +
            "AND d.status = 'ONLINE' " +
            "GROUP BY d.country.code")
    List<Object[]> getDeviceCountByCountry(@Param("userId") UUID userId);

    // ===== MAINTENANCE =====

    /**
     * Trouver les devices offline depuis longtemps
     */
    @Query("SELECT d FROM Device d WHERE d.status = 'OFFLINE' AND d.lastHeartbeatAt < :threshold")
    List<Device> findStaleDevices(@Param("threshold") Instant threshold);

    /**
     * Marquer un device comme offline
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.status = 'OFFLINE' WHERE d.id = :deviceId")
    void markAsOffline(@Param("deviceId") UUID deviceId);
}