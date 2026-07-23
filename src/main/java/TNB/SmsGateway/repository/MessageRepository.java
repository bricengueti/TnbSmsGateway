package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.entity.MessageDirection;
import TNB.SmsGateway.entity.MessageStatus;
import TNB.SmsGateway.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // ===== FIND =====

    /**
     * Trouver tous les messages d'un utilisateur (paginated)
     */
    Page<Message> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Trouver tous les messages d'un utilisateur par status
     */
    List<Message> findByUserAndStatus(User user, MessageStatus status);

    /**
     * Trouver les messages en attente ou dispatchés
     */
    @Query("SELECT m FROM Message m WHERE m.status IN ('PENDING', 'DISPATCHED') AND m.attempts < 3")
    List<Message> findPendingMessages();

    /**
     * Trouver les messages à réassigner (device offline)
     */
    @Query("SELECT m FROM Message m WHERE m.status = 'DISPATCHED' " +
            "AND m.device.id = :deviceId AND m.attempts < 3")
    List<Message> findDispatchMessagesByDevice(@Param("deviceId") UUID deviceId);

    /**
     * Trouver les messages avec webhook non livré et tentatives < max
     */
    @Query("SELECT m FROM Message m WHERE m.direction = 'INBOUND' " +
            "AND m.webhookDeliveredAt IS NULL AND m.attempts < 5")
    List<Message> findMessagesWithPendingWebhook();

    /**
     * Trouver un message par idempotency key
     */
    Optional<Message> findByUserAndIdempotencyKey(User user, String idempotencyKey);

    /**
     * Trouver les messages d'un utilisateur par direction
     */
    Page<Message> findByUserAndDirection(User user, MessageDirection direction, Pageable pageable);

    /**
     * Trouver les messages d'un utilisateur par pays
     */
    Page<Message> findByUserAndCountryCode(User user, String countryCode, Pageable pageable);

    /**
     * Trouver les messages d'un utilisateur par opérateur
     */
    Page<Message> findByUserAndOperatorCode(User user, String operatorCode, Pageable pageable);

    /**
     * Recherche filtrée des messages d'un utilisateur (écran "Activity Logs").
     * Chaque filtre est optionnel : si le paramètre est null, la condition
     * correspondante est ignorée (comportement "pas de filtre").
     * La recherche porte sur le numéro de destination et le contenu du message.
     */
    @Query("SELECT m FROM Message m WHERE m.user = :user " +
            "AND (:direction IS NULL OR m.direction = :direction) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:search IS NULL OR LOWER(m.toNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(m.fromNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(m.body) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> searchByUser(@Param("user") User user,
                               @Param("direction") MessageDirection direction,
                               @Param("status") MessageStatus status,
                               @Param("search") String search,
                               Pageable pageable);

    // ===== UPDATE =====

    /**
     * Mettre à jour le status d'un message
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = :status WHERE m.id = :messageId")
    void updateStatus(@Param("messageId") UUID messageId, @Param("status") MessageStatus status);

    /**
     * Mettre à jour le status avec device et SIM
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = :status, " +
            "m.device.id = :deviceId, " +
            "m.deviceSim.id = :simId, " +
            "m.dispatchedAt = :dispatchedAt " +
            "WHERE m.id = :messageId")
    void dispatchMessage(@Param("messageId") UUID messageId,
                         @Param("status") MessageStatus status,
                         @Param("deviceId") UUID deviceId,
                         @Param("simId") UUID simId,
                         @Param("dispatchedAt") Instant dispatchedAt);

    /**
     * Marquer un message comme délivré
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'DELIVERED', m.deliveredAt = :deliveredAt WHERE m.id = :messageId")
    void markAsDelivered(@Param("messageId") UUID messageId, @Param("deliveredAt") Instant deliveredAt);

    /**
     * Marquer un message comme échoué
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'FAILED', m.errorReason = :reason, m.attempts = m.attempts + 1 " +
            "WHERE m.id = :messageId")
    void markAsFailed(@Param("messageId") UUID messageId, @Param("reason") String reason);

    /**
     * Incrémenter les tentatives
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.attempts = m.attempts + 1 WHERE m.id = :messageId")
    void incrementAttempts(@Param("messageId") UUID messageId);

    /**
     * Marquer le webhook comme livré
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.webhookDeliveredAt = :deliveredAt WHERE m.id = :messageId")
    void markWebhookDelivered(@Param("messageId") UUID messageId, @Param("deliveredAt") Instant deliveredAt);

    // ===== STATS =====

    /**
     * Compter les messages par status pour un utilisateur
     */
    @Query("SELECT m.status, COUNT(m) FROM Message m WHERE m.user.id = :userId GROUP BY m.status")
    List<Object[]> countMessagesByStatus(@Param("userId") UUID userId);

    /**
     * Compter les messages envoyés aujourd'hui par utilisateur
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.user.id = :userId " +
            "AND m.direction = 'OUTBOUND' AND m.createdAt > :startOfDay")
    long countSentToday(@Param("userId") UUID userId, @Param("startOfDay") Instant startOfDay);

    /**
     * Compter les messages reçus aujourd'hui par utilisateur
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.user.id = :userId " +
            "AND m.direction = 'INBOUND' AND m.createdAt > :startOfDay")
    long countReceivedToday(@Param("userId") UUID userId, @Param("startOfDay") Instant startOfDay);

    /**
     * Statistiques de livraison par utilisateur
     */
    @Query("SELECT COUNT(m), SUM(CASE WHEN m.status = 'DELIVERED' THEN 1 ELSE 0 END) " +
            "FROM Message m WHERE m.user.id = :userId AND m.direction = 'OUTBOUND'")
    List<Object[]> getDeliveryStats(@Param("userId") UUID userId);

    // ===== MAINTENANCE =====

    /**
     * Supprimer les messages EXPIRED
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.status = 'EXPIRED' AND m.createdAt < :threshold")
    int deleteExpiredMessages(@Param("threshold") Instant threshold);

    /**
     * Marquer les messages PENDING trop vieux comme EXPIRED
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'EXPIRED' " +
            "WHERE m.status = 'PENDING' AND m.createdAt < :threshold")
    void expireOldPendingMessages(@Param("threshold") Instant threshold);
}