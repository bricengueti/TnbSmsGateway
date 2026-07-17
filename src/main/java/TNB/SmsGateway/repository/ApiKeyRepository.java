package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.ApiKey;
import TNB.SmsGateway.entity.ApiKeyScope;
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
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    /**
     * Trouver une clé par son hash
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * Trouver toutes les clés d'un utilisateur
     */
    List<ApiKey> findByUserId(UUID userId);

    /**
     * Trouver toutes les clés actives d'un utilisateur (non révoquées)
     */
    List<ApiKey> findByUserIdAndRevokedAtIsNull(UUID userId);

    /**
     * Trouver les clés par scope
     */
    List<ApiKey> findByScope(ApiKeyScope scope);

    /**
     * Trouver les clés expirées (avec date de révocation)
     */
    @Query("SELECT a FROM ApiKey a WHERE a.revokedAt IS NOT NULL AND a.revokedAt < :since")
    List<ApiKey> findRevokedSince(@Param("since") Instant since);

    /**
     * Révoquer une clé
     */
    @Modifying
    @Transactional
    @Query("UPDATE ApiKey a SET a.revokedAt = :revokedAt WHERE a.id = :id")
    void revokeKey(@Param("id") UUID id, @Param("revokedAt") Instant revokedAt);

    /**
     * Révoquer toutes les clés d'un utilisateur
     */
    @Modifying
    @Transactional
    @Query("UPDATE ApiKey a SET a.revokedAt = :revokedAt WHERE a.user.id = :userId AND a.revokedAt IS NULL")
    void revokeAllUserKeys(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    /**
     * Vérifier si une clé existe et est active
     */
    @Query("SELECT COUNT(a) > 0 FROM ApiKey a WHERE a.keyHash = :keyHash AND a.revokedAt IS NULL")
    boolean existsActiveKey(@Param("keyHash") String keyHash);

    /**
     * Compter les clés actives d'un utilisateur
     */
    @Query("SELECT COUNT(a) FROM ApiKey a WHERE a.user.id = :userId AND a.revokedAt IS NULL")
    long countActiveKeysByUser(@Param("userId") UUID userId);
}