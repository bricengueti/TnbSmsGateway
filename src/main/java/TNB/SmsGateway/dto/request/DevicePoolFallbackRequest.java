package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Active ou désactive un device PERSONAL comme secours pour le pool partagé")
public record DevicePoolFallbackRequest(
        @Schema(description = "true = ce device peut servir le pool si aucun device POOL pur n'est " +
                "disponible. N'affecte jamais le quota SMS de son propriétaire.", example = "true")
        @NotNull(message = "enabled est obligatoire")
        Boolean enabled
) {}