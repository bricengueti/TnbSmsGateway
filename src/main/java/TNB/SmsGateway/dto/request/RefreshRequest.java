package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête de rafraîchissement du token JWT")
public record RefreshRequest(
        @Schema(description = "Refresh token JWT",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le refresh token est obligatoire")
        String refreshToken
) {}