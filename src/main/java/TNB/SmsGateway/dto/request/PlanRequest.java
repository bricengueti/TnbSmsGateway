package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Requête de création d'un pack (plan tarifaire) pour les clients en mode pool partagé")
public record PlanRequest(
        @Schema(description = "Nom commercial du pack, affiché aux clients", example = "Pro")
        @NotBlank(message = "Le nom du pack est obligatoire")
        String name,

        @Schema(description = "Description courte du pack", example = "5 000 SMS/mois, support email")
        String description,

        @Schema(description = "Limite mensuelle de SMS. Laisser vide (null) pour un pack illimité.",
                example = "5000")
        @Positive(message = "La limite doit être positive")
        Integer monthlySmsLimit,

        @Schema(description = "Prix mensuel indicatif (affichage uniquement, aucune facturation " +
                "automatique n'est déclenchée par ce champ)", example = "15000")
        java.math.BigDecimal priceMonthly
) {}