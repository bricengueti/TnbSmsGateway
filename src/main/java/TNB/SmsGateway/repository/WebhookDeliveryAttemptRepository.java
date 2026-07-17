package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.entity.WebhookDeliveryAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookDeliveryAttemptRepository extends JpaRepository<WebhookDeliveryAttempt, UUID> {

    /**
     * Trouver toutes les tentatives pour un message (triées par numéro)
     */
    List<WebhookDeliveryAttempt> findByMessageOrderByAttemptNumberAsc(Message message);

    /**
     * Trouver la dernière tentative pour un message
     */
    @Query("SELECT w FROM WebhookDeliveryAttempt w WHERE w.message = :message ORDER BY w.attemptNumber DESC LIMIT 1")
    WebhookDeliveryAttempt findLastAttempt(@Param("message") Message message);

    /**
     * Compter les tentatives pour un message
     */
    @Query("SELECT COUNT(w) FROM WebhookDeliveryAttempt w WHERE w.message = :message")
    long countAttempts(@Param("message") Message message);

    /**
     * Compter les tentatives échouées pour un message
     */
    @Query("SELECT COUNT(w) FROM WebhookDeliveryAttempt w WHERE w.message = :message AND w.success = false")
    long countFailedAttempts(@Param("message") Message message);

    /**
     * Trouver les messages avec échecs récents
     */
    @Query("SELECT w.message FROM WebhookDeliveryAttempt w " +
            "WHERE w.success = false AND w.attemptedAt > :since " +
            "GROUP BY w.message HAVING COUNT(w) >= 3")
    List<Message> findMessagesWithRecentFailures(@Param("since") Instant since);

    /**
     * Taux de succès par client
     */
    @Query("SELECT m.user.id, " +
            "COUNT(w), " +
            "SUM(CASE WHEN w.success = true THEN 1 ELSE 0 END) " +
            "FROM WebhookDeliveryAttempt w " +
            "JOIN w.message m " +
            "GROUP BY m.user.id")
    List<Object[]> getSuccessRateByUser();

    /**
     * Dernières tentatives échouées
     */
    @Query("SELECT w FROM WebhookDeliveryAttempt w WHERE w.success = false ORDER BY w.attemptedAt DESC")
    List<WebhookDeliveryAttempt> findRecentFailures(Pageable pageable);

    /**
     * Compter les tentatives par status
     */
    @Query("SELECT COUNT(w), w.success FROM WebhookDeliveryAttempt w GROUP BY w.success")
    List<Object[]> countBySuccess();

    /**
     * Compter les tentatives par code HTTP
     */
    @Query("SELECT w.httpStatus, COUNT(w) FROM WebhookDeliveryAttempt w " +
            "WHERE w.httpStatus IS NOT NULL GROUP BY w.httpStatus ORDER BY COUNT(w) DESC")
    List<Object[]> countByHttpStatus();
}