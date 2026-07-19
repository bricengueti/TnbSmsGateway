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

    List<DeviceSim> findByDevice(Device device);

    Optional<DeviceSim> findByDeviceAndSlotIndex(Device device, Integer slotIndex);

    @Query("SELECT s FROM DeviceSim s WHERE s.device = :device AND s.isActive = true")
    List<DeviceSim> findActiveSims(@Param("device") Device device);

    // 🔥 SUPPRIMÉ: findSimsWithQuota (JPQL ne peut plus comparer dailySmsSent < dailySmsQuota,
    // ce dernier étant désormais un String pouvant valoir "ILLIMITE").
    // Remplacé par filtrage Java ci-dessous, basé sur findActiveSims() + hasQuota().

    /**
     * Retourne les SIMs actives ayant encore du quota disponible
     * (illimité OU dailySmsSent < quota numérique).
     * Filtrage fait en Java car dailySmsQuota est un String ("ILLIMITE" possible).
     */
    default List<DeviceSim> findSimsWithQuota(Device device) {
        return findActiveSims(device).stream()
                .filter(DeviceSim::hasQuota)
                .toList();
    }

    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.isActive = :active WHERE s.id = :simId")
    void updateActive(@Param("simId") UUID simId, @Param("active") Boolean active);

    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsSent = s.dailySmsSent + 1 WHERE s.id = :simId")
    void incrementDailySmsSent(@Param("simId") UUID simId);

    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsSent = 0")
    void resetDailyCounters();

    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsSent = 0 WHERE s.device.id = :deviceId")
    void resetDailyCountersForDevice(@Param("deviceId") UUID deviceId);

    // 🔥 SUPPRIMÉ: findSimsWithQuotaExceeded (même problème que findSimsWithQuota).
    // Remplacé par un filtrage Java sur toutes les SIMs actives.

    /**
     * Retourne les SIMs actives dont le quota est dépassé (jamais vrai si illimité).
     * Filtrage fait en Java pour la même raison que findSimsWithQuota.
     */
    default List<DeviceSim> findSimsWithQuotaExceeded() {
        return findAll().stream()
                .filter(sim -> Boolean.TRUE.equals(sim.getIsActive()))
                .filter(sim -> !sim.hasQuota())
                .toList();
    }

    // 🔥 CORRIGÉ: Integer → String, pour matcher le type réel du champ
    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.dailySmsQuota = :quota WHERE s.id = :simId")
    void updateQuota(@Param("simId") UUID simId, @Param("quota") String quota);

    @Modifying
    @Transactional
    @Query("UPDATE DeviceSim s SET s.phoneNumber = :phoneNumber WHERE s.id = :simId")
    void updatePhoneNumber(@Param("simId") UUID simId, @Param("phoneNumber") String phoneNumber);

    @Query("SELECT s.device.id, COUNT(s) FROM DeviceSim s WHERE s.isActive = true GROUP BY s.device.id")
    List<Object[]> countActiveSimsByDevice();
}