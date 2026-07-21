package az.ilham.ecommerceauth.dto.user;

import java.util.Set;

public record UserProfileDto(
        Long id,
        String username,
        String email,
        String phoneNumber,
        String firstName,
        String lastName,
        boolean emailVerified,
        boolean phoneVerified,
        Set<String> roles
) {
}
