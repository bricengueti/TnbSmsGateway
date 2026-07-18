package TNB.SmsGateway.config;

import TNB.SmsGateway.security.ApiKeyAuthenticationFilter;
import TNB.SmsGateway.security.JwtAuthenticationFilter;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          ApiKeyAuthenticationFilter apiKeyAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // ===== PUBLIC =====
                        .requestMatchers(
                                "/v1/auth/**",
                                "/v1/devices/pair",
                                "/v1/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/actuator/health"
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

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}