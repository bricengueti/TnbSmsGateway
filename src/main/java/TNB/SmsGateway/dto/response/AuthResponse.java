package TNB.SmsGateway.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'authentification")
public record AuthResponse(
        @Schema(description = "Token d'accès JWT (expire dans 12h)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Refresh token JWT (expire dans 7 jours)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,


        @Schema(description = "Indique si le compte vient d'être créé", example = "false")
        boolean isNewAccount
) {}

