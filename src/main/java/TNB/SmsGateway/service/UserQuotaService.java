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
     * SCÉNARIO: Consommer un crédit SMS avant dispatch (routingMode=MANAGED_POOL)
     * Modèle PRÉPAYÉ : pas de reset périodique, le solde ne baisse qu'à l'usage.
     * Absence de quota configuré = bloqué par défaut (fail-safe, inchangé depuis la V1).
     */
    @Transactional
    public void consumeCredit(UUID userId) {
        UserQuota quota = userQuotaRepository.findByUserIdAndType(userId, PlanType.POOL)
                .orElseThrow(QuotaNotConfiguredException::new);

        if (!quota.hasRemainingCredits()) {
            throw new QuotaExceededException();
        }

        quota.consumeCredit();
        userQuotaRepository.save(quota);
    }

    /**
     * SCÉNARIO: Vérifier le plafond de devices avant pairing (targetType=PERSONAL)
     * Absence de quota PERSONAL configuré = AUCUNE restriction (comportement historique
     * préservé — contrairement au POOL, ne pas casser les comptes BYOD existants).
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

    /**
     * Récupère (ou crée à la volée, non persisté) le quota d'un type pour un user,
     * utilisé par l'admin pour afficher/ajuster même si rien n'a encore été assigné.
     */
    public UserQuota getOrCreateTransient(User user, PlanType type) {
        return userQuotaRepository.findByUserIdAndType(user.getId(), type)
                .orElseGet(() -> new UserQuota(user, type));
    }
}