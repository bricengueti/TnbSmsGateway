package TNB.SmsGateway.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Requête d'envoi de SMS en masse")
public record SendBulkMessageRequest(
        @Schema(description = "Liste des messages à envoyer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "La liste des messages ne peut pas être vide")
        @Size(max = 100, message = "Maximum 100 messages par requête")
        @Valid
        List<BulkMessageItem> messages
) {}

