package TNB.SmsGateway.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * SERVICE: ScheduleService
 *
 * DESCRIPTION: Gère toutes les tâches planifiées du système
 * - Réinitialisation des quotas quotidiens (minuit)
 * - Détection des devices offline (toutes les minutes)
 * - Nettoyage des OTPs expirés (toutes les heures)
 *
 * SCÉNARIOS:
 * 1. Quota: réinitialiser daily_sms_sent à 0 chaque jour
 * 2. Offline: détecter les devices sans heartbeat > 2min
 * 3. OTP: supprimer les OTPs expirés
 */
@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final DeviceSimService deviceSimService;
    private final DeviceStatusService deviceStatusService;
    private final OtpService otpService;

    public ScheduleService(DeviceSimService deviceSimService,
                           DeviceStatusService deviceStatusService,
                           OtpService otpService) {
        this.deviceSimService = deviceSimService;
        this.deviceStatusService = deviceStatusService;
        this.otpService = otpService;
    }

    /**
     * SCÉNARIO: Réinitialiser les compteurs de SMS quotidiens
     * Exécution: Tous les jours à minuit
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailySmsCounters() {
        log.info("Réinitialisation des compteurs quotidiens de SMS");
        deviceSimService.resetDailyCounters();
    }

    /**
     * SCÉNARIO: Détecter les devices offline
     * Exécution: Toutes les minutes
     */
    @Scheduled(fixedDelay = 60000)
    public void checkStaleDevices() {
        log.debug("Vérification des devices offline");
        deviceStatusService.handleStaleDevices();
    }

    /**
     * SCÉNARIO: Nettoyer les OTPs expirés
     * Exécution: Toutes les heures
     */
    @Scheduled(fixedDelay = 3600000)
    public void cleanExpiredOtps() {
        log.debug("Nettoyage des OTPs expirés");
        otpService.cleanExpiredOtps();
    }
}