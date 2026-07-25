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

        @Schema(description = "Description courte", example = "5 000 crédits SMS")
        String description,

        @Schema(description = "Type de pack. POOL = crédits SMS prépayés pour le pool partagé. " +
                "PERSONAL = plafond de devices pour les clients BYOD.",
                allowableValues = {"POOL", "PERSONAL"}, example = "POOL")
        @NotNull(message = "Le type de pack est obligatoire")
        String type,

        @Schema(description = "Nombre de crédits SMS inclus dans ce pack (type=POOL uniquement). " +
                "Laisser vide pour un pack illimité.", example = "5000")
        @Positive(message = "Les crédits doivent être positifs")
        Integer smsCredits,

        @Schema(description = "Nombre maximum de devices autorisés (type=PERSONAL uniquement). " +
                "Laisser vide pour un pack sans limite de devices.", example = "5")
        @Positive(message = "Le nombre de devices doit être positif")
        Integer maxDevices,

        @Schema(description = "Prix indicatif du pack (affichage uniquement)", example = "15000")
        BigDecimal price
) {}