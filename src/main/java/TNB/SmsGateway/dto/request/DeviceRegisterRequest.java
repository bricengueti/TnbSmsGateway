package TNB.SmsGateway.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête d'enregistrement d'un device")
public record DeviceRegisterRequest(
        @Schema(description = "Nom du device", example = "Device Cameroun 1")
        @NotBlank(message = "Le label est obligatoire")
        @Size(max = 100, message = "Le label ne doit pas dépasser 100 caractères")
        String label,

        @Schema(description = "Code pays ISO 3166-1 alpha-2", example = "CM")
        @NotBlank(message = "Le code pays est obligatoire")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Le code pays doit être au format ISO 3166-1 (2 lettres majuscules)")
        String countryCode
) {}