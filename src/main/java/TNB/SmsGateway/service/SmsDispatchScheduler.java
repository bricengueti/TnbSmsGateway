package TNB.SmsGateway.service;

import TNB.SmsGateway.websocket.handler.DeviceWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * SERVICE: SmsDispatchScheduler
 *
 * DESCRIPTION: Espace l'envoi réel des commandes DISPATCH_SMS vers chaque
 * device/SIM, pour simuler un rythme d'envoi humain et éviter la détection
 * d'un pattern robotisé par les opérateurs.
 *
 * PRINCIPE : événementiel, pas de polling. Une tâche ponctuelle est
 * programmée exactement à l'instant où chaque SIM redevient disponible.
 *
 * ⚠️ LIMITATION CONNUE : file en mémoire (pas persistée en base). Si le
 * serveur redémarre avec des messages encore en attente de dispatch réseau,
 * ces commandes sont perdues de cette file (le Message reste en base avec
 * DISPATCHED déjà posé, mais ne sera jamais transmis au device tant qu'un
 * mécanisme de réconciliation au démarrage n'est pas ajouté).
 */
@Service
public class SmsDispatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(SmsDispatchScheduler.class);

    private final TaskScheduler taskScheduler;
    private final DeviceWebSocketHandler webSocketHandler;

    private final ConcurrentHashMap<UUID, Queue<PendingDispatch>> simQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Instant> simNextAvailableAt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> simTaskScheduled = new ConcurrentHashMap<>();

    public SmsDispatchScheduler(@Qualifier("smsDispatchTaskScheduler") TaskScheduler taskScheduler,
                                @Lazy DeviceWebSocketHandler webSocketHandler) {
        this.taskScheduler = taskScheduler;
        this.webSocketHandler = webSocketHandler;
    }

    private record PendingDispatch(
            UUID deviceId,
            String slotIndex,
            String messageId,
            String to,
            String body
    ) {}

    public void scheduleDispatch(UUID deviceId, UUID simId, int minDelaySec, int maxDelaySec,
                                 String slotIndex, String messageId, String to, String body) {
        PendingDispatch dispatch = new PendingDispatch(deviceId, slotIndex, messageId, to, body);

        Queue<PendingDispatch> queue = simQueues.computeIfAbsent(simId, k -> new ConcurrentLinkedQueue<>());
        queue.add(dispatch);

        log.debug("📥 SMS {} mis en file pour SIM {} (taille file={})", messageId, simId, queue.size());

        tryScheduleNext(simId, minDelaySec, maxDelaySec);
    }

    private void tryScheduleNext(UUID simId, int minDelaySec, int maxDelaySec) {
        if (Boolean.TRUE.equals(simTaskScheduled.putIfAbsent(simId, true))) {
            return;
        }

        Queue<PendingDispatch> queue = simQueues.get(simId);
        if (queue == null || queue.isEmpty()) {
            simTaskScheduled.remove(simId);
            return;
        }

        Instant nextAvailableAt = simNextAvailableAt.getOrDefault(simId, Instant.now());
        Instant runAt = nextAvailableAt.isBefore(Instant.now()) ? Instant.now() : nextAvailableAt;

        taskScheduler.schedule(() -> processNext(simId, minDelaySec, maxDelaySec), runAt);
    }

    private void processNext(UUID simId, int minDelaySec, int maxDelaySec) {
        simTaskScheduled.remove(simId);

        Queue<PendingDispatch> queue = simQueues.get(simId);
        if (queue == null) return;

        PendingDispatch dispatch = queue.poll();
        if (dispatch == null) return;

        try {
            webSocketHandler.dispatchSms(
                    dispatch.deviceId(),
                    dispatch.slotIndex(),
                    dispatch.messageId(),
                    dispatch.to(),
                    dispatch.body()
            );
            log.info("📡 SMS {} dispatché (SIM {})", dispatch.messageId(), simId);
        } catch (Exception e) {
            log.error("❌ Échec du dispatch programmé pour SMS {}", dispatch.messageId(), e);
        }

        int delaySec = minDelaySec >= maxDelaySec
                ? minDelaySec
                : ThreadLocalRandom.current().nextInt(minDelaySec, maxDelaySec + 1);
        simNextAvailableAt.put(simId, Instant.now().plusSeconds(delaySec));

        if (!queue.isEmpty()) {
            tryScheduleNext(simId, minDelaySec, maxDelaySec);
        }
    }
}