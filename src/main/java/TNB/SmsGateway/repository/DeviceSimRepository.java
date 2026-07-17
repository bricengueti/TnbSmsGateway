package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceSim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceSimRepository extends JpaRepository<DeviceSim, UUID> {

    /**
     * Trouver toutes les SIMs d'un device
     */
    List<DeviceSim> findByDevice(Device device);

    /**
     * Trouver toutes les SIMs d'un device par slot
     */
    Optional<DeviceSim> findByDeviceAndSlotIndex(Device device, Integer slotIndex);

    /**
     * Trouver toutes les SIMs actives d'un device
     */
    @Query("SELECT s FROM DeviceSim s WHERE s.device = :device AND s.isActive = true")
    List<DeviceSim> findActiveSims(@Param("device") Device device);

    /**
     * Trouver les SIMs avec quota restant
     */
    @Query("SELECT s FROM DeviceSim s WHERE s.device = :device AND s.isActive = true AND s.dailySmsSent < s.dailySmsQuota")
    List<DeviceSim> findSimsWithQuota(@Param("device") Device device);

    /**
     * Mettre à jour l'activité d'une SIM
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.isActive = :active WHERE s.id = :simId")
    void updateActive(@Param("simId") UUID simId, @Param("active") Boolean active);

    /**
     * Incrémenter le compteur de SMS envoyés
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsSent = s.dailySmsSent + 1 WHERE s.id = :simId")
    void incrementDailySmsSent(@Param("simId") UUID simId);

    /**
     * Réinitialiser les compteurs quotidiens
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsSent = 0")
    void resetDailyCounters();

    /**
     * Réinitialiser les compteurs pour un device spécifique
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsSent = 0 WHERE s.device.id = :deviceId")
    void resetDailyCountersForDevice(@Param("deviceId") UUID deviceId);

    /**
     * Trouver les SIMs avec quota dépassé
     */
    @Query("SELECT s FROM DeviceSim s WHERE s.isActive = true AND s.dailySmsSent >= s.dailySmsQuota")
    List<DeviceSim> findSimsWithQuotaExceeded();

    /**
     * Mettre à jour le quota d'une SIM
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsQuota = :quota WHERE s.id = :simId")
    void updateQuota(@Param("simId") UUID simId, @Param("quota") Integer quota);

    /**
     * Mettre à jour le numéro de téléphone
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.phoneNumber = :phoneNumber WHERE s.id = :simId")
    void updatePhoneNumber(@Param("simId") UUID simId, @Param("phoneNumber") String phoneNumber);

    /**
     * Compter les SIMs actives par device
     */
    @Query("SELECT s.device.id, COUNT(s) FROM DeviceSim s WHERE s.isActive = true GROUP BY s.device.id")
    List<Object[]> countActiveSimsByDevice();
}