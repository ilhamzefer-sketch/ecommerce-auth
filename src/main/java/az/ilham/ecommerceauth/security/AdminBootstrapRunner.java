package az.ilham.ecommerceauth.security;

import az.ilham.ecommerceauth.user.entity.Role;
import az.ilham.ecommerceauth.user.entity.RoleName;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.RoleRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }

        if (!StringUtils.hasText(properties.username())
                || !StringUtils.hasText(properties.email())
                || !StringUtils.hasText(properties.password())) {
            log.warn("Admin bootstrap skipped because username/email/password are not fully configured.");
            return;
        }

        if (userRepository.findByUsername(properties.username()).isPresent()
                || userRepository.findByEmail(properties.email()).isPresent()) {
            log.info("Bootstrap admin already exists, skipping admin seed.");
            return;
        }

        Set<Role> roles = new LinkedHashSet<>();
        roles.add(findRole(RoleName.USER));
        roles.add(findRole(RoleName.ADMIN));

        User admin = User.builder()
                .username(properties.username())
                .email(properties.email())
                .passwordHash(passwordEncoder.encode(properties.password()))
                .firstName(properties.firstName())
                .lastName(properties.lastName())
                .enabled(true)
                .accountNonLocked(true)
                .emailVerified(true)
                .roles(roles)
                .build();

        userRepository.save(admin);
        log.info("Bootstrap admin user created: {}", admin.getUsername());
    }

    private Role findRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Required role not found: " + roleName));
    }
}
