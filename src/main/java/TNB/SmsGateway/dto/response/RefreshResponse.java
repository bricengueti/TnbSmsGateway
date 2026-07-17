package TNB.SmsGateway.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse de rafraîchissement du token")
public record RefreshResponse(
        @Schema(description = "Nouveau token d'accès JWT (expire dans 12h)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {}