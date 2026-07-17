package TNB.SmsGateway.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de pairing d'un device")
public record DevicePairRequest(
        @Schema(description = "Code de pairing reçu lors de l'enregistrement",
                example = "123456")
        @NotBlank(message = "Le code de pairing est obligatoire")
        @Size(min = 6, max = 6, message = "Le code de pairing doit contenir exactement 6 chiffres")
        @Pattern(regexp = "^[0-9]{6}$", message = "Le code de pairing doit contenir uniquement des chiffres")
        String pairingCode
) {}