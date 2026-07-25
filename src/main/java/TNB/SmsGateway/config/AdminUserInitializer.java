package TNB.SmsGateway.config;

import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    public AdminUserInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                existing -> {
                    if (!existing.isAdmin()) {
                        existing.setAdmin(true);
                        userRepository.save(existing);
                        log.info("✅ Compte existant promu admin: {}", adminEmail);
                    } else {
                        log.info("✅ Compte admin déjà présent: {}", adminEmail);
                    }
                },
                () -> {
                    User admin = new User(adminEmail);
                    admin.setAdmin(true);
                    admin.setCompanyName("Administration");
                    userRepository.save(admin);
                    log.info("🔧 Compte admin créé par défaut: {}", adminEmail);
                }
        );
    }
}