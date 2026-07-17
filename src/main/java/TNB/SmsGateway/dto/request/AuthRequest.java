package TNB.SmsGateway.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête d'authentification OTP")
public record AuthRequest(
        @Schema(description = "Email de l'utilisateur", example = "user@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email
) {}