package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.PlanType;
import TNB.SmsGateway.entity.UserQuota;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.exception.device.DeviceLimitExceededException;
import TNB.SmsGateway.exception.message.QuotaExceededException;
import TNB.SmsGateway.exception.message.QuotaExpiredException;
import TNB.SmsGateway.exception.message.QuotaNotConfiguredException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.repository.UserQuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     *
     * ⚠️ CHANGEMENT DE MODÈLE : le quota ne se réinitialise plus tout seul au
     * changement de mois civil. La période de validité est désormais fixée
     * une fois pour toutes à l'assignation du pack (voir UserQuota.startNewPeriod),
     * pour une durée de plan.validityMonths. Passé periodEndsAt, le pack est
     * simplement expiré — il faut le réassigner/renouveler (assignPoolPlan),
     * pas de reset automatique silencieux.
     * Absence de quota configuré = bloqué par défaut (fail-safe, inchangé).
     */
    @Transactional
    public void consumeQuota(UUID userId) {
        UserQuota quota = userQuotaRepository.findByUserIdAndType(userId, PlanType.POOL)
                .orElseThrow(QuotaNotConfiguredException::new);

        if (quota.isExpired()) {
            throw new QuotaExpiredException();
        }

        if (!quota.hasRemainingQuota()) {
            throw new QuotaExceededException();
        }

        quota.incrementUsage();
        userQuotaRepository.save(quota);
    }

    /**
     * SCÉNARIO: Vérifier le plafond de devices avant pairing (targetType=PERSONAL)
     * Absence de quota PERSONAL configuré = AUCUNE restriction (préserve les comptes BYOD existants).
     * Un pack PERSONAL expiré bloque le pairing de NOUVEAUX devices (les
     * devices déjà pairés continuent de fonctionner normalement).
     */
    public void checkDeviceLimitOrThrow(UUID userId) {
        userQuotaRepository.findByUserIdAndType(userId, PlanType.PERSONAL).ifPresent(quota -> {
            if (quota.isUnlimited()) {
                return;
            }
            if (quota.isExpired()) {
                throw new BusinessException(
                        "Votre pack PERSONAL a expiré. Renouvelez-le pour pairer de nouveaux devices.",
                        "PERSONAL_QUOTA_EXPIRED", 403);
            }
            if (quota.getQuantityDevices() == null) {
                return;
            }
            long currentCount = deviceRepository.countPersonalDevicesByUserId(userId);
            if (currentCount >= quota.getQuantityDevices()) {
                throw new DeviceLimitExceededException(quota.getQuantityDevices());
            }
        });
    }
}
