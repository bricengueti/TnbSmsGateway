package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de mise à jour d'un device")
public record DeviceUpdateRequest(
        @Schema(description = "Nouveau nom du device", example = "Device Cameroun 1 - Principal")
        @Size(max = 100, message = "Le label ne doit pas dépasser 100 caractères")
        String label,

        @Schema(description = "Nouveau code pays", example = "SN")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Le code pays doit être au format ISO 3166-1 (2 lettres majuscules)")
        String countryCode
) {}