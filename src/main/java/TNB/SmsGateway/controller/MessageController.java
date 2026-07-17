package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.SendBulkMessageRequest;
import TNB.SmsGateway.dto.request.SendMessageRequest;
import TNB.SmsGateway.dto.response.MessageResponse;
import TNB.SmsGateway.dto.common.PageResponse;
import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CONTROLLER: MessageController
 *
 * DESCRIPTION: Gère l'envoi et la consultation des SMS
 * URL de base: /api/v1/messages (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. Envoi d'un SMS unique avec pays/opérateur
 * 2. Envoi de SMS en masse (max 100)
 * 3. Consultation du status d'un message
 * 4. Liste paginée des messages
 */
@RestController
@RequestMapping("/v1/messages")
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Messages", description = "Gestion des SMS (envoi et consultation)")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // =============================================
    // ===== ENVOI UNIQUE =====
    // =============================================

    /**
     * SCÉNARIO: Le client envoie un SMS unique
     *
     * URL: POST /api/v1/messages/send
     * Auth: ApiKey (SEND_ONLY ou FULL)
     *
     * RÉPONSES:
     * - 202: SMS accepté pour envoi
     * - 400: Erreur de validation
     * - 422: Aucun device disponible
     * - 401: Clé API invalide
     * - 403: Scope insuffisant
     */
    @Operation(
            summary = "Envoyer un SMS",
            description = "Envoie un SMS via un device disponible pour le pays et l'opérateur spécifiés. Le numéro doit être au format E.164."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "SMS accepté pour envoi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Erreur de validation (numéro invalide, opérateur/pays incompatible)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Aucun device disponible pour le pays/opérateur demandé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Clé API invalide ou manquante"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Permissions insuffisantes (scope incorrect)"
            )
    })
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        MessageResponse response = messageService.sendMessage(userId, request);
        return ResponseEntity.accepted().body(response);
    }

    // =============================================
    // ===== ENVOI EN MASSE =====
    // =============================================

    /**
     * SCÉNARIO: Le client envoie des SMS en masse
     *
     * URL: POST /api/v1/messages/send-bulk
     * Auth: ApiKey (SEND_ONLY ou FULL)
     *
     * RÉPONSES:
     * - 202: SMS acceptés pour envoi
     * - 400: Erreur de validation
     * - 422: Aucun device disponible
     */
    @Operation(
            summary = "Envoyer des SMS en masse",
            description = "Envoie jusqu'à 100 SMS simultanément. Chaque message est traité indépendamment."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "SMS acceptés pour envoi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Erreur de validation"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Aucun device disponible pour certains messages"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Clé API invalide ou manquante"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Permissions insuffisantes (scope incorrect)"
            )
    })
    @PostMapping("/send-bulk")
    public ResponseEntity<List<MessageResponse>> sendBulkMessages(
            @Valid @RequestBody SendBulkMessageRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<MessageResponse> responses = request.messages().stream()
                .map(item -> {
                    SendMessageRequest singleRequest = new SendMessageRequest(
                            item.to(),
                            item.body(),
                            item.countryCode(),
                            item.operator(),
                            "NORMAL",
                            null
                    );
                    return messageService.sendMessage(userId, singleRequest);
                })
                .collect(Collectors.toList());

        return ResponseEntity.accepted().body(responses);
    }

    // =============================================
    // ===== CONSULTATION D'UN MESSAGE =====
    // =============================================

    /**
     * SCÉNARIO: Le client consulte le status d'un message
     *
     * URL: GET /api/v1/messages/{id}
     * Auth: ApiKey (READ_ONLY ou FULL)
     */
    @Operation(
            summary = "Consulter un message",
            description = "Retourne les détails d'un message spécifique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Message trouvé",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Message non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Clé API invalide ou manquante"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Permissions insuffisantes (scope incorrect)"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessage(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        Message message = messageService.findByIdAndUser(id, userId);
        return ResponseEntity.ok(toMessageResponse(message));
    }

    // =============================================
    // ===== LISTE DES MESSAGES =====
    // =============================================

    /**
     * SCÉNARIO: Le client consulte la liste de ses messages
     *
     * URL: GET /api/v1/messages?page=0&size=20
     * Auth: ApiKey (READ_ONLY ou FULL)
     */
    @Operation(
            summary = "Lister les messages",
            description = "Retourne la liste paginée des messages de l'utilisateur."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Liste récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Clé API invalide ou manquante"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Permissions insuffisantes (scope incorrect)"
            )
    })
    @GetMapping
    public ResponseEntity<PageResponse<MessageResponse>> listMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        Page<Message> messagePage = messageService.getMessagesByUser(userId, page, size);

        List<MessageResponse> data = messagePage.getContent().stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        PageResponse<MessageResponse> response = new PageResponse<>(
                data,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalElements(),
                messagePage.getTotalPages(),
                messagePage.isLast()
        );

        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== UTILITAIRES =====
    // =============================================

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId().toString(),
                message.getDirection().name(),
                message.getToNumber(),
                message.getFromNumber(),
                message.getBody(),
                message.getCountryCode(),
                message.getOperatorCode(),
                message.getStatus().name(),
                message.getAttempts(),
                message.getErrorReason(),
                message.getDevice() != null ? message.getDevice().getId().toString() : null,
                message.getCreatedAt(),
                message.getDispatchedAt(),
                message.getDeliveredAt()
        );
    }
}