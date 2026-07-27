package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.PlanType;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.entity.UserQuota;
import TNB.SmsGateway.exception.device.DeviceLimitExceededException;
import TNB.SmsGateway.exception.message.QuotaExceededException;
import TNB.SmsGateway.exception.message.QuotaNotConfiguredException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.repository.UserQuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class UserQuotaService {

    private final UserQuotaRepository userQuotaRepository;
    private final DeviceRepository deviceRepository;

    public UserQuotaService(UserQuotaRepository userQuotaRepository, DeviceRepository deviceRepository) {
        this.userQuotaRepository = userQuotaRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * SCÉNARIO: Consommer une unité de quota avant dispatch (routingMode=MANAGED_POOL)
     * Modèle CLASSIQUE : reset automatique au changement de mois (comme les SIM/DeviceSim).
     * Absence de quota configuré = bloqué par défaut (fail-safe, inchangé).
     */
    @Transactional
    public void consumeQuota(UUID userId) {
        UserQuota quota = userQuotaRepository.findByUserIdAndType(userId, PlanType.POOL)
                .orElseThrow(QuotaNotConfiguredException::new);

        resetIfNewPeriod(quota);

        if (!quota.hasRemainingQuota()) {
            throw new QuotaExceededException();
        }

        quota.incrementUsage();
        userQuotaRepository.save(quota);
    }

    private void resetIfNewPeriod(UserQuota quota) {
        Instant now = Instant.now();
        if (quota.getResetAt() == null || now.isAfter(quota.getResetAt())) {
            Instant nextReset = LocalDate.now(ZoneOffset.UTC)
                    .withDayOfMonth(1).plusMonths(1)
                    .atStartOfDay(ZoneOffset.UTC).toInstant();
            quota.resetForNewPeriod(nextReset);
        }
    }

    /**
     * SCÉNARIO: Vérifier le plafond de devices avant pairing (targetType=PERSONAL)
     * Absence de quota PERSONAL configuré = AUCUNE restriction (préserve les comptes BYOD existants).
     */
    public void checkDeviceLimitOrThrow(UUID userId) {
        userQuotaRepository.findByUserIdAndType(userId, PlanType.PERSONAL).ifPresent(quota -> {
            if (quota.isUnlimited() || quota.getMaxDevices() == null) {
                return;
            }
            long currentCount = deviceRepository.countPersonalDevicesByUserId(userId);
            if (currentCount >= quota.getMaxDevices()) {
                throw new DeviceLimitExceededException(quota.getMaxDevices());
            }
        });
    }
}