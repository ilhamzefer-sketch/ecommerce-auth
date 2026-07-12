package az.ilham.ecommerceauth.user.service;

import az.ilham.ecommerceauth.common.exception.ResourceNotFoundException;
import az.ilham.ecommerceauth.dto.user.UpdateUserRolesRequest;
import az.ilham.ecommerceauth.dto.user.UserSummaryResponse;
import az.ilham.ecommerceauth.user.entity.Role;
import az.ilham.ecommerceauth.user.entity.RoleName;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.RoleRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getUserById(Long userId) {
        return toSummary(findUser(userId));
    }

    @Transactional
    public UserSummaryResponse updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        User user = findUser(userId);

        Set<String> normalizedRoleNames = new LinkedHashSet<>(request.getRoles());
        normalizedRoleNames.add(RoleName.USER);

        Set<Role> roles = normalizedRoleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + roleName)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        user.setRoles(roles);
        userRepository.save(user);

        return toSummary(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserSummaryResponse toSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
