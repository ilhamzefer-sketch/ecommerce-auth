package az.ilham.ecommerceauth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.bootstrap.admin")
public record AdminBootstrapProperties(
        boolean enabled,
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) {
}
