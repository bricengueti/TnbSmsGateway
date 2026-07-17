package TNB.SmsGateway.repository;
import TNB.SmsGateway.entity.OtpCode;
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
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    /**
     * Trouver un OTP par email (le plus récent)
     */
    @Query("SELECT o FROM OtpCode o WHERE o.email = :email ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpCode> findLatestByEmail(@Param("email") String email);



    /**
     * Trouver un OTP par email et code hash
     */
    Optional<OtpCode> findByEmailAndCodeHash(String email, String codeHash);

    /**
     * Trouver tous les OTPs non expirés pour un email
     */
    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.expiresAt > :now AND o.verifiedAt IS NULL")
    List<OtpCode> findValidOtpsByEmail(@Param("email") String email, @Param("now") Instant now);

    /**
     * Marquer un OTP comme vérifié
     */
    @Modifying
    @Transactional
    @Query("UPDATE OtpCode o SET o.verifiedAt = :verifiedAt WHERE o.id = :id")
    void markAsVerified(@Param("id") UUID id, @Param("verifiedAt") Instant verifiedAt);

    /**
     * Incrémenter les tentatives
     */
    @Modifying
    @Transactional
    @Query("UPDATE OtpCode o SET o.attempts = o.attempts + 1 WHERE o.id = :id")
    void incrementAttempts(@Param("id") UUID id);

    /**
     * Supprimer les OTPs expirés
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") Instant now);

    /**
     * Supprimer les OTPs pour un email
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpCode o WHERE o.email = :email")
    void deleteByEmail(@Param("email") String email);

    /**
     * Compter les tentatives pour un email
     */
    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.email = :email AND o.createdAt > :since")
    long countAttemptsSince(@Param("email") String email, @Param("since") Instant since);
}