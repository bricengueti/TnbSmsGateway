package TNB.SmsGateway.security;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentifie les requêtes REST provenant du device Android lui-même (pas d'un
 * utilisateur connecté au dashboard, pas d'une intégration externe avec clé API).
 *
 * Réutilise les identifiants déjà utilisés pour la connexion WebSocket
 * (deviceId + secretToken), envoyés ici en headers HTTP :
 *   X-Device-Id: <UUID du device>
 *   X-Device-Secret: <secretToken en clair, vérifié en BCrypt côté serveur>
 *
 * Une fois le device authentifié, on résout son propriétaire (device.getUser())
 * et on charge un UserPrincipal exactement comme le font JwtAuthenticationFilter
 * et ApiKeyAuthenticationFilter, afin que les controllers en aval (ex: MessageController)
 * n'aient besoin d'aucune logique spécifique pour ce mode d'authentification.
 */
@Component
public class DeviceAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DeviceAuthenticationFilter.class);

    private static final String HEADER_DEVICE_ID = "X-Device-Id";
    private static final String HEADER_DEVICE_SECRET = "X-Device-Secret";

    private final DeviceRepository deviceRepository;
    private final CustomUserDetailsService userDetailsService;

    public DeviceAuthenticationFilter(DeviceRepository deviceRepository,
                                      CustomUserDetailsService userDetailsService) {
        this.deviceRepository = deviceRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String deviceIdHeader = request.getHeader(HEADER_DEVICE_ID);
        String deviceSecretHeader = request.getHeader(HEADER_DEVICE_SECRET);

        // Rien à faire ici si les headers device ne sont pas présents : on laisse
        // la place au JwtAuthenticationFilter / ApiKeyAuthenticationFilter (headers différents).
        if (deviceIdHeader == null || deviceSecretHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Si une authentification a déjà été posée par un filtre précédent, on ne l'écrase pas.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID deviceId = UUID.fromString(deviceIdHeader);

            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (deviceOpt.isEmpty()) {
                log.warn("❌ DeviceAuthFilter - Device introuvable: {}", deviceId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"INVALID_DEVICE_CREDENTIALS\"}");
                return;
            }

            Device device = deviceOpt.get();

            boolean isValid = device.getSecretTokenHash() != null &&
                    org.springframework.security.crypto.bcrypt.BCrypt.checkpw(
                            deviceSecretHeader,
                            device.getSecretTokenHash()
                    );

            if (!isValid) {
                log.warn("❌ DeviceAuthFilter - Secret invalide pour device {}", deviceId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"INVALID_DEVICE_CREDENTIALS\"}");
                return;
            }

            if (device.getUser() == null) {
                log.error("❌ DeviceAuthFilter - Device {} sans utilisateur propriétaire", deviceId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"INVALID_DEVICE_CREDENTIALS\"}");
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserById(device.getUser().getId());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("✅ DeviceAuthFilter - Authentifié via device {}, userId={}", deviceId, device.getUser().getId());

        } catch (IllegalArgumentException e) {
            log.warn("❌ DeviceAuthFilter - X-Device-Id invalide: {}", deviceIdHeader);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"INVALID_DEVICE_CREDENTIALS\"}");
            return;
        } catch (Exception e) {
            log.error("❌ DeviceAuthFilter - Erreur inattendue", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"INVALID_DEVICE_CREDENTIALS\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}