package az.ilham.ecommerceauth.security;

import az.ilham.ecommerceauth.user.entity.Role;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.RoleRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.bootstrap-admin.enabled:false}")
    private boolean enabled;

    @Value("${application.bootstrap-admin.username:admin}")
    private String username;

    @Value("${application.bootstrap-admin.email:admin@mizan.local}")
    private String email;

    @Value("${application.bootstrap-admin.phone-number:+994500000000}")
    private String phoneNumber;

    @Value("${application.bootstrap-admin.password:ChangeMe123!}")
    private String password;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        User admin = User.builder()
                .username(username)
                .email(email.toLowerCase())
                .phoneNumber(phoneNumber)
                .passwordHash(passwordEncoder.encode(password))
                .firstName("Mizan")
                .lastName("Admin")
                .enabled(true)
                .accountNonLocked(true)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(userRole, adminRole))
                .build();
        userRepository.save(admin);
    }
}
