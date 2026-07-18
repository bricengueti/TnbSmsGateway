package TNB.SmsGateway.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TNB SMS Gateway API",
                version = "1.0.0",
                description = "Plateforme SaaS d'envoi/réception de SMS multi-pays, multi-opérateur",
                contact = @Contact(
                        name = "TNB Support",
                        email = "support@tnb.com",
                        url = "https://tnb.com"
                ),
                license = @License(
                        name = "Propriétaire",
                        url = "https://tnb.com/license"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8070/api", description = "Développement local")
        },
        security = {
                @SecurityRequirement(name = "BearerAuth"),
                @SecurityRequirement(name = "ApiKeyAuth")
        }
)
// ✅ JWT — Swagger ajoute automatiquement "Bearer "
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = """
                🔑 Session dashboard.
                Collez uniquement le token JWT (sans 'Bearer ').
                Swagger ajoutera automatiquement le préfixe.
                Exemple: eyJhbGciOiJIUzI1NiJ9...
                """
)
// ✅ Clé API — même mécanisme HTTP bearer : Swagger ajoute "Bearer " automatiquement.
// Le backend distingue JWT vs clé API via le préfixe du token lui-même (tnb_ vs le reste).
@SecurityScheme(
        name = "ApiKeyAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "API Key",
        in = SecuritySchemeIn.HEADER,
        description = """
                🔑 Intégration technique.
                Collez uniquement la clé API (sans 'Bearer ').
                Swagger ajoutera automatiquement le préfixe.
                Exemple: tnb_live_7f2k9xq1m3p5v8d2c4a6
                """
)
public class SwaggerConfig {
    // Aucun code nécessaire - tout est géré par les annotations
}