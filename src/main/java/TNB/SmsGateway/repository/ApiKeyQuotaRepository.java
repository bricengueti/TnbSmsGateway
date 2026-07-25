package TNB.SmsGateway.repository;


import TNB.SmsGateway.entity.ApiKeyQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyQuotaRepository extends JpaRepository<ApiKeyQuota, UUID> {
    Optional<ApiKeyQuota> findByApiKeyId(UUID apiKeyId);
}