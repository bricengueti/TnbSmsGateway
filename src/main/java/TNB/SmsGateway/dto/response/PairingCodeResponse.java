package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse de régénération du code de connexion")
public record PairingCodeResponse(
        @Schema(description = "Nouveau code de connexion (AFFICHÉ UNE SEULE FOIS)", example = "738204")
        String pairingCode
) {}