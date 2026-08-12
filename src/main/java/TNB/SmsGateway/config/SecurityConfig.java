package TNB.SmsGateway.config;

import TNB.SmsGateway.security.ApiKeyAuthenticationFilter;
import TNB.SmsGateway.security.DeviceAuthenticationFilter;
import TNB.SmsGateway.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthFilter;
    private final DeviceAuthenticationFilter deviceAuthFilter;

    // Liste blanche d'origines, une par environnement — voir cors.allowed-origins
    // dans application-{profile}.properties. Ne JAMAIS mettre "*" ici : le
    // frontend envoie un Authorization: Bearer, donc les credentials CORS
    // sont implicitement en jeu et "*" est de toute façon rejeté par les
    // navigateurs dès qu'un header Authorization personnalisé est présent
    // en preflight.
//    @Value("${cors.allowed-origins}")
//    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          ApiKeyAuthenticationFilter apiKeyAuthFilter,
                          DeviceAuthenticationFilter deviceAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.deviceAuthFilter = deviceAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // ===== PUBLIC =====
                        .requestMatchers(
                                "/v1/auth/**",
                                "/v1/devices/pair",
                                // ✅ Cadence d'envoi : appelée directement par l'app mobile,
                                // qui n'a pas de JWT utilisateur (seulement deviceId/secretToken
                                // pour le WebSocket). Le userId est retrouvé côté service via
                                // device.getUser().getId(), pas besoin d'authentification ici.
                                "/v1/devices/*/pacing",
                                "/v1/webhook/payment/**",
                                "/v1/devices/*/sims/*/pacing",
                                "/v1/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/actuator/health",
                                "/ws/**"
                        ).permitAll()
                        // ===== JWT - Dashboard =====
                        .requestMatchers(
                                "/v1/webhook/**",
                                "/v1/api-keys/**",
                                "/v1/devices/register",
                                "/v1/devices/**",
                                "/v1/reference/**"
                        ).authenticated()

                        // ===== API KEY - Integration =====
                        .requestMatchers(HttpMethod.POST, "/v1/messages/send").authenticated()
                        .requestMatchers(HttpMethod.POST, "/v1/messages/send-bulk").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/messages/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/coverage").authenticated()

                        // ===== ADMIN =====
                        .requestMatchers("/admin/**").hasRole("ADMIN")   // ✅ ajouté
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(deviceAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Autorise le frontend Angular (dashboard) à appeler l'API depuis un
     * navigateur. Sans ce bean, Spring Security n'ajoute AUCUN header
     * Access-Control-Allow-Origin — même les routes publiques (permitAll)
     * sont bloquées côté navigateur, car le préflight OPTIONS échoue en
     * amont de la logique d'autorisation.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        List<String> origins = Arrays.stream(allowedOrigins.split(","))
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .toList();
        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Signature"));
        configuration.setAllowCredentials(false); // Bearer en header, pas de cookies — pas besoin des credentials CORS
        configuration.setMaxAge(3600L); // met le résultat du preflight en cache côté navigateur 1h

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
