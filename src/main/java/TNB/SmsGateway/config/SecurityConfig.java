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

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

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
                // Active CORS avec configuration custom
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Désactive CSRF (API stateless)
                .csrf(csrf -> csrf.disable())
                // Pas de session côté serveur
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Définition des règles d’autorisation
                .authorizeHttpRequests(authz -> authz
                        // ===== PUBLIC =====
                        .requestMatchers(
                                "/v1/auth/**",
                                "/v1/devices/pair",
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
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // ===== PAR DÉFAUT =====
                        .anyRequest().authenticated()
                )
                // Ajout des filtres custom
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(deviceAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS : autorise uniquement les origines définies
     * dans application-{profile}.properties via cors.allowed-origins
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // autorise toutes les origines
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Signature"));
        configuration.setAllowCredentials(false); // pas de cookies, uniquement Bearer
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
