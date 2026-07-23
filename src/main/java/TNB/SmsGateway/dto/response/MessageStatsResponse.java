package TNB.SmsGateway.dto.response;

/**
 * Statistiques agrégées des messages d'un utilisateur, pour l'écran Dashboard.
 *
 * totalDelivered ne compte que les messages OUTBOUND au statut DELIVERED
 * (un message INBOUND n'a pas de notion de "livraison" côté émetteur).
 * sentToday / receivedToday sont calculés sur la journée courante (UTC).
 */
public record MessageStatsResponse(
        long totalSent,
        long totalReceived,
        long totalFailed,
        long totalDelivered,
        long sentToday,
        long receivedToday
) {
}