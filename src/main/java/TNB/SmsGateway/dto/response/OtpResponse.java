package TNB.SmsGateway.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'envoi d'OTP")
public record OtpResponse(
        @Schema(description = "Message de confirmation", example = "OTP envoyé avec succès")
        String message,

        @Schema(description = "Email de destination", example = "user@company.com")
        String email,

        @Schema(description = "Temps de validité (minutes)", example = "5")
        Integer expiresInMinutes
) {}