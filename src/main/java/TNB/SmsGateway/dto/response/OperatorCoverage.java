package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Couverture d'un opérateur")
public record OperatorCoverage(
        @Schema(description = "Code opérateur", example = "MTN_CM")
        String operatorCode,

        @Schema(description = "Nom de l'opérateur", example = "MTN")
        String operatorName,

        @Schema(description = "Nombre de devices disponibles", example = "2")
        Integer availableDevices,

        @Schema(description = "Nombre de SIMs actives", example = "4")
        Integer activeSims,

        @Schema(description = "Capacité totale de SMS par jour", example = "400")
        Integer dailyCapacity
) {}
