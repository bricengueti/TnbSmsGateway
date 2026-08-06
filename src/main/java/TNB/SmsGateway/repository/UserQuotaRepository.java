package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.PlanType;
import TNB.SmsGateway.entity.UserQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserQuotaRepository extends JpaRepository<UserQuota, UUID> {
    Optional<UserQuota> findByUserIdAndType(UUID userId, PlanType type);
}