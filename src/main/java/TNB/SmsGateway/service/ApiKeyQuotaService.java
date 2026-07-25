package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.ApiKeyQuota;
import TNB.SmsGateway.exception.message.QuotaExceededException;
import TNB.SmsGateway.exception.message.QuotaNotConfiguredException;
import TNB.SmsGateway.repository.ApiKeyQuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class ApiKeyQuotaService {

    private final ApiKeyQuotaRepository apiKeyQuotaRepository;

    public ApiKeyQuotaService(ApiKeyQuotaRepository apiKeyQuotaRepository) {
        this.apiKeyQuotaRepository = apiKeyQuotaRepository;
    }

    /**
     * SCÉNARIO: Vérifier et réserver une unité de quota avant dispatch (mode MANAGED_POOL)
     * ÉTAPES:
     * 1. Charger le quota de la clé — absent = bloqué (fail-safe)
     * 2. Réinitialiser si on est entré dans un nouveau mois
     * 3. Vérifier le quota restant, sinon lever QuotaExceededException
     * 4. Incrémenter et sauvegarder
     */
    @Transactional
    public void checkAndReserve(UUID apiKeyId) {
        ApiKeyQuota quota = apiKeyQuotaRepository.findByApiKeyId(apiKeyId)
                .orElseThrow(QuotaNotConfiguredException::new);

        resetIfNewPeriod(quota);

        if (!quota.hasRemainingQuota()) {
            throw new QuotaExceededException();
        }

        quota.increment();
        apiKeyQuotaRepository.save(quota);
    }

    private void resetIfNewPeriod(ApiKeyQuota quota) {
        Instant now = Instant.now();
        if (quota.getResetAt() == null || now.isAfter(quota.getResetAt())) {
            Instant nextReset = LocalDate.now(ZoneOffset.UTC)
                    .withDayOfMonth(1).plusMonths(1)
                    .atStartOfDay(ZoneOffset.UTC).toInstant();
            quota.resetForNewPeriod(nextReset);
        }
    }
}