package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.SendBulkMessageRequest;
import TNB.SmsGateway.dto.request.SendMessageRequest;
import TNB.SmsGateway.dto.response.MessageResponse;
import TNB.SmsGateway.dto.common.PageResponse;
import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.security.UserPrincipal;
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

@RestController
@RequestMapping("/v1/messages")
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Messages", description = "Gestion des SMS (envoi et consultation)")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(
            summary = "Envoyer un SMS",
            description = "Envoie un SMS via un device disponible pour le pays et l'opérateur spécifiés."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "SMS accepté pour envoi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        // 🔥 Récupérer l'ID correctement
        UUID userId = getUserIdFromAuthentication(authentication);
        MessageResponse response = messageService.sendMessage(userId, request);
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<List<MessageResponse>> sendBulkMessages(
            @Valid @RequestBody SendBulkMessageRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
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

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessage(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        Message message = messageService.findByIdAndUser(id, userId);
        return ResponseEntity.ok(toMessageResponse(message));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MessageResponse>> listMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        Page<Message> messagePage = messageService.searchMessagesByUser(
                userId, page, size, direction, status, search);

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

    // 🔥 MÉTHODE UTILITAIRE POUR EXTRAIRE L'ID
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        throw new RuntimeException("Impossible d'extraire l'ID utilisateur");
    }

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