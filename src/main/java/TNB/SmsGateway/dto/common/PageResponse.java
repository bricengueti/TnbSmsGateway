package TNB.SmsGateway.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Réponse paginée")
public record PageResponse<T>(
        @Schema(description = "Liste des données")
        List<T> data,

        @Schema(description = "Numéro de la page actuelle", example = "0")
        int page,

        @Schema(description = "Taille de la page", example = "20")
        int size,

        @Schema(description = "Nombre total d'éléments", example = "150")
        long totalElements,

        @Schema(description = "Nombre total de pages", example = "8")
        int totalPages,

        @Schema(description = "Dernière page", example = "false")
        boolean last
) {}