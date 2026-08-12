package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Requête de création d'un pack tarifaire")
public record PlanRequest(
        @Schema(description = "Nom commercial du pack", example = "Pro")
        @NotBlank(message = "Le nom du pack est obligatoire")
        String name,

        @Schema(description = "Description courte", example = "10 000 SMS sur 3 mois")
        String description,

        @Schema(description = "Type de pack. POOL = quota de SMS pour le pool partagé. " +
                "PERSONAL = plafond de devices pour les clients BYOD.",
                allowableValues = {"POOL", "PERSONAL"}, example = "POOL")
        @NotNull(message = "Le type de pack est obligatoire")
        String type,

        @Schema(description = "Durée de validité du pack en mois (ex: 1, 3, 12)", example = "3")
        @NotNull(message = "La durée de validité est obligatoire")
        @Positive(message = "La durée de validité doit être positive")
        Integer validityMonths,

        @Schema(description = "Quota total de SMS pour toute la période (type=POOL uniquement, " +
                "pas de reset mensuel). Laisser vide pour un pack illimité.", example = "10000")
        @Positive(message = "Le quota SMS doit être positif")
        Integer quantitySms,

        @Schema(description = "Nombre maximum de devices simultanés autorisés (type=PERSONAL " +
                "uniquement). Laisser vide pour un pack sans limite de devices.", example = "5")
        @Positive(message = "Le nombre de devices doit être positif")
        Integer quantityDevices,

        @Schema(description = "Prix indicatif du pack (affichage uniquement)", example = "15000")
        BigDecimal price
) {}
