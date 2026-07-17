package TNB.SmsGateway.service;


import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.entity.WebhookDeliveryAttempt;
import TNB.SmsGateway.exception.message.WebhookDeliveryException;
import TNB.SmsGateway.repository.MessageRepository;
import TNB.SmsGateway.repository.WebhookDeliveryAttemptRepository;
import TNB.SmsGateway.utils.JsonUtils;
import TNB.SmsGateway.utils.SignatureUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * SERVICE: WebhookService
 *
 * DESCRIPTION: Gère l'envoi des SMS entrants aux clients via webhook
 * - Envoi des SMS entrants à l'URL configurée
 * - Signature HMAC-SHA256 (X-TNB-Signature)
 * - Enregistrement des tentatives
 * - Retry en cas d'échec
 *
 * SCÉNARIOS:
 * 1. Envoi: SMS entrant → webhook du client
 * 2. Signature: payload signé avec le secret du client
 * 3. Échec: enregistrement de la tentative
 * 4. Test: payload de test pour valider la configuration
 */
@Service
public class WebhookService {

    private final RestTemplate restTemplate;
    private final MessageRepository messageRepository;
    private final WebhookDeliveryAttemptRepository attemptRepository;
    private final SignatureUtils signatureUtils;

    @Value("${webhook.timeout:5000}")
    private int timeout;

    public WebhookService(RestTemplate restTemplate,
                          MessageRepository messageRepository,
                          WebhookDeliveryAttemptRepository attemptRepository,
                          SignatureUtils signatureUtils) {
        this.restTemplate = restTemplate;
        this.messageRepository = messageRepository;
        this.attemptRepository = attemptRepository;
        this.signatureUtils = signatureUtils;
    }

    /**
     * SCÉNARIO: Envoyer un SMS entrant au webhook du client
     * ÉTAPES:
     * 1. Vérifier que le webhook est configuré
     * 2. Préparer le payload (messageId, from, body, etc.)
     * 3. Générer la signature HMAC-SHA256
     * 4. Envoyer la requête HTTP avec signature
     * 5. Enregistrer la tentative
     * 6. Si succès → marquer le message comme livré
     * 7. Si échec → retry plus tard
     *
     * @param message Message entrant
     * @param user Utilisateur destinataire
     */
    @Async("webhookExecutor")
    @Transactional
    public void sendToWebhook(Message message, User user) {
        // Vérifier si webhook configuré
        if (user.getWebhookUrl() == null || user.getWebhookUrl().isEmpty()) {
            return;
        }

        try {
            // 1. Préparer le payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("messageId", message.getId().toString());
            payload.put("from", message.getFromNumber());
            payload.put("to", message.getToNumber());
            payload.put("body", message.getBody());
            payload.put("countryCode", message.getCountryCode());
            payload.put("operator", message.getOperatorCode());
            payload.put("receivedAt", message.getCreatedAt().toString());

            String payloadJson = JsonUtils.toJson(payload);

            // 2. Générer la signature
            String signature = signatureUtils.generateWebhookSignature(payloadJson, user.getWebhookSecret());

            // 3. Préparer la requête
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-TNB-Signature", signature);

            HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);

            // 4. Envoyer
            ResponseEntity<String> response = restTemplate.postForEntity(
                    user.getWebhookUrl(),
                    entity,
                    String.class
            );

            // 5. Enregistrer la tentative
            WebhookDeliveryAttempt attempt = new WebhookDeliveryAttempt();
            attempt.setMessage(message);
            attempt.setAttemptNumber(message.getAttempts() + 1);
            attempt.setHttpStatus(response.getStatusCode().value());
            attempt.setSuccess(response.getStatusCode().is2xxSuccessful());
            attempt.setAttemptedAt(Instant.now());

            attemptRepository.save(attempt);

            // 6. Si succès, marquer le message
            if (response.getStatusCode().is2xxSuccessful()) {
                message.markWebhookDelivered();
                messageRepository.save(message);
            } else {
                // Échec → retry plus tard
                throw new WebhookDeliveryException(message.getId(), attempt.getAttemptNumber());
            }

        } catch (Exception e) {
            // Enregistrer l'échec
            WebhookDeliveryAttempt attempt = new WebhookDeliveryAttempt();
            attempt.setMessage(message);
            attempt.setAttemptNumber(message.getAttempts() + 1);
            attempt.setHttpStatus(null);
            attempt.setSuccess(false);
            attempt.setAttemptedAt(Instant.now());

            attemptRepository.save(attempt);

            // Retry sera géré par un scheduler
            message.incrementAttempts();
            messageRepository.save(message);
        }
    }

    /**
     * SCÉNARIO: Tester le webhook
     * ÉTAPES:
     * 1. Vérifier que le webhook est configuré
     * 2. Envoyer un payload de test
     * 3. Signer la requête
     * 4. Retourner true si le serveur répond 2xx
     *
     * @param user Utilisateur
     * @return true si le test est réussi
     */
    @Transactional
    public boolean testWebhook(User user) {
        if (user.getWebhookUrl() == null || user.getWebhookUrl().isEmpty()) {
            return false;
        }

        try {
            // Payload de test
            Map<String, Object> payload = new HashMap<>();
            payload.put("test", true);
            payload.put("messageId", "test-" + System.currentTimeMillis());
            payload.put("from", "+237600000000");
            payload.put("to", "+237611111111");
            payload.put("body", "Ceci est un message de test");
            payload.put("countryCode", "CM");
            payload.put("operator", "MTN_CM");
            payload.put("timestamp", Instant.now().toString());

            String payloadJson = JsonUtils.toJson(payload);

            // Signature
            String signature = signatureUtils.generateWebhookSignature(payloadJson, user.getWebhookSecret());

            // Envoyer
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-TNB-Signature", signature);

            HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    user.getWebhookUrl(),
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            return false;
        }
    }
}