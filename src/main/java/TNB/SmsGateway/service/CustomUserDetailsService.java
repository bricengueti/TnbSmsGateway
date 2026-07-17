package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.repository.UserRepository;
import TNB.SmsGateway.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("🔍 Chargement de l'utilisateur par email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ Utilisateur non trouvé avec l'email: {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
                });

        log.info("✅ Utilisateur trouvé: id={}, email={}", user.getId(), user.getEmail());

        return new UserPrincipal(user);
    }

    public UserDetails loadUserById(UUID userId) {
        log.info("🔍 Chargement de l'utilisateur par ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("❌ Utilisateur non trouvé avec l'ID: {}", userId);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + userId);
                });

        log.info("✅ Utilisateur trouvé: id={}, email={}", user.getId(), user.getEmail());

        return new UserPrincipal(user);
    }
}