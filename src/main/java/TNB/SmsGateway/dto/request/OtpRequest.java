package TNB.SmsGateway.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de vérification OTP")
public record OtpRequest(
        @Schema(description = "Email de l'utilisateur", example = "user@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @Schema(description = "Code OTP à 6 chiffres reçu par email", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le code OTP est obligatoire")
        @Size(min = 6, max = 6, message = "Le code OTP doit contenir exactement 6 chiffres")
        @Pattern(regexp = "^[0-9]{6}$", message = "Le code OTP doit contenir uniquement des chiffres")
        String code
) {}