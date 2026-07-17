package TNB.SmsGateway.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Réponse d'erreur standardisée")
public record ErrorResponse(
        @Schema(description = "Horodatage de l'erreur", example = "2026-07-16T14:30:00Z")
        Instant timestamp,

        @Schema(description = "Code HTTP", example = "422")
        int status,

        @Schema(description = "Code d'erreur", example = "NO_DEVICE_FOR_COUNTRY_OPERATOR")
        String error,

        @Schema(description = "Message d'erreur", example = "Aucun device disponible pour le couple CM/MTN_CM")
        String message,

        @Schema(description = "Chemin de la requête", example = "/api/v1/messages/send")
        String path,

        @Schema(description = "Erreurs de validation des champs")
        Map<String, String> validationErrors
) {}