package TNB.SmsGateway.dto.common;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse API générique")
public record ApiResponse(
        @Schema(description = "Message de confirmation", example = "Opération réussie")
        String message,

        @Schema(description = "Succès de l'opération", example = "true")
        boolean success
) {}