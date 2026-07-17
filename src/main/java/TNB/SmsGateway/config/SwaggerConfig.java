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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TNB SMS Gateway API",
                version = "1.0.0",
                description = """
                        # 📱 Plateforme SaaS d'envoi/réception de SMS multi-pays, multi-opérateur

                        ## 🔐 Authentification
                        - **JWT (Bearer Authentication)**: Utilisé pour le dashboard (expire après 12h)
                        - **ApiKey (API Key Authentication)**: Utilisé pour l'intégration technique (révocable)

                        ## 📋 Flux principaux
                        1. **Authentification** → OTP par email
                        2. **Gestion des devices** → Enregistrement et pairing
                        3. **Envoi de SMS** → Avec routage pays/opérateur
                        4. **Réception de SMS** → Via webhook

                        ## 🔑 Scopes API Key
                        - `FULL`: Accès complet
                        - `SEND_ONLY`: Envoi de SMS uniquement
                        - `READ_ONLY`: Consultation uniquement
                        """,
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
                @Server(url = "/api", description = "Serveur principal TNB SMS Gateway"),
                @Server(url = "/api-preprod", description = "Serveur de pré-production"),
                @Server(url = "http://localhost:8080/api", description = "Serveur de développement")
        },
        security = {
                @SecurityRequirement(name = "Bearer Authentication"),
                @SecurityRequirement(name = "API Key Authentication")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = """
                JWT Token pour l'authentification du dashboard.
                
                **Comment l'obtenir:**
                1. POST /auth/otp/request → recevoir un OTP par email
                2. POST /auth/otp/verify → obtenir le JWT
                
                **Durée de vie:** 12 heures
                """,
        in = SecuritySchemeIn.HEADER
)
@SecurityScheme(
        name = "API Key Authentication",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "Authorization",
        description = """
                Clé API pour l'intégration technique.
                
                **Format:** `Bearer tnb_live_xxxxxxxxxxxxxxxxxxxx`
                
                **Comment l'obtenir:**
                1. S'authentifier avec JWT
                2. POST /api-keys → générer une clé
                
                **Scopes disponibles:**
                - `FULL`: Accès complet
                - `SEND_ONLY`: Envoi de SMS uniquement
                - `READ_ONLY`: Consultation uniquement
                
                **Révocation:** DELETE /api-keys/{id}
                """
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customizeOpenAPI() {
        // Security Scheme pour JWT (Bearer)
        io.swagger.v3.oas.models.security.SecurityScheme bearerScheme =
                new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description("""
                                JWT Token pour le dashboard.
                                Format: Bearer <votre-token-jwt>
                                """);

        // Security Scheme pour API Key
        io.swagger.v3.oas.models.security.SecurityScheme apiKeyScheme =
                new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description("""
                                Clé API pour l'intégration technique.
                                Format: Bearer tnb_live_xxxxxxxxxxxxxxxxxxxx
                                """);

        // Security Requirements
        io.swagger.v3.oas.models.security.SecurityRequirement bearerRequirement =
                new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("Bearer Authentication");

        io.swagger.v3.oas.models.security.SecurityRequirement apiKeyRequirement =
                new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("API Key Authentication");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", bearerScheme)
                        .addSecuritySchemes("API Key Authentication", apiKeyScheme))
                .addSecurityItem(bearerRequirement)
                .addSecurityItem(apiKeyRequirement);
    }
}