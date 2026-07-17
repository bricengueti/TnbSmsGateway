package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informations de l'utilisateur")
public record UserInfo(
        @Schema(description = "ID de l'utilisateur", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Email de l'utilisateur", example = "user@company.com")
        String email,

        @Schema(description = "Nom de l'entreprise", example = "Ma Société")
        String companyName,

        @Schema(description = "Statut du compte", example = "ACTIVE")
        String status
) {}
