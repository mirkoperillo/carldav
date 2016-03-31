package carldav.bootstrap;

import carldav.entity.User;
import carldav.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Kamill Sokol
 */
@Component
public class AdminUserCreator implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = getLogger(AdminUserCreator.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminName;
    private final String adminPassword;

    public AdminUserCreator(UserRepository userRepository, PasswordEncoder passwordEncoder, @Value("${carldav.admin.name}") String adminName, @Value("${carldav.admin.password}") String adminPassword) {
        Assert.notNull(userRepository, "userRepository is null");
        Assert.notNull(passwordEncoder, "passwordEncoder is null");
        Assert.notNull(adminName, "adminName is null");
        Assert.notNull(adminPassword, "adminPassword is null");
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminPassword = adminPassword;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        LOG.info("checking for admin user with name {}", adminName);

        User admin = userRepository.findByEmailIgnoreCase(adminName);

        if(admin != null) {
            LOG.info("admin user found. checking admin user password for '{}'", adminName);

            if(!admin.getPassword().equals(adminPassword)) {
                LOG.info("admin user password changed. setting to '{}", adminPassword);
                admin.setPassword(passwordEncoder.encodePassword(adminPassword, null));
                userRepository.save(admin);
                LOG.info("admin user password changed");
            }
        } else {
            LOG.info("admin user '{}' not found creating", adminName);

            admin = new User();
            admin.setEmail(adminName);
            admin.setPassword(passwordEncoder.encodePassword(adminPassword, null));
            admin.setRole("ROLE_ADMIN");
            admin.setLocked(false);
            userRepository.save(admin);
        }

        LOG.info("admin user '{}:{}'", admin.getEmail(), admin.getPassword());
    }
}
