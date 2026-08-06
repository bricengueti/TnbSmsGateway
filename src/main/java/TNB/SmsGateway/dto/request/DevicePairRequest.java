package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de pairing d'un device")
public record DevicePairRequest(
        @Schema(description = "Code de connexion du compte (réutilisable sur plusieurs devices)",
                example = "123456")
        @NotBlank(message = "Le code de connexion est obligatoire")
        @Size(min = 6, max = 6, message = "Le code de connexion doit contenir exactement 6 chiffres")
        @Pattern(regexp = "^[0-9]{6}$", message = "Le code de connexion doit contenir uniquement des chiffres")
        String pairingCode,

        // ✅ Ajouté : pays détecté automatiquement par l'app Android (countryIso
        // de la SIM active, via SimDetector) — remplace la saisie manuelle du
        // pays qui se faisait auparavant côté dashboard dans DeviceRegisterRequest.
        @Schema(description = "Code pays ISO 3166-1 alpha-2 détecté sur la SIM du téléphone", example = "CM")
        @NotBlank(message = "Le pays détecté est obligatoire")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Le code pays doit être au format ISO 3166-1 (2 lettres majuscules)")
        String countryIso,

        // ✅ Ajouté : optionnel, pour distinguer plusieurs téléphones pairés
        // avec le même code (ex: "Téléphone Orange", "Téléphone MTN salon")
        @Schema(description = "Nom donné à ce téléphone (optionnel)", example = "Téléphone Orange - bureau")
        @Size(max = 100, message = "Le label ne doit pas dépasser 100 caractères")
        String deviceLabel
) {}