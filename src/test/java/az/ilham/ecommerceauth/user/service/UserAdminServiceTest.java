package az.ilham.ecommerceauth.user.service;

import az.ilham.ecommerceauth.dto.user.UpdateUserRolesRequest;
import az.ilham.ecommerceauth.dto.user.UserSummaryResponse;
import az.ilham.ecommerceauth.user.entity.Role;
import az.ilham.ecommerceauth.user.entity.RoleName;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.RoleRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(10L)
                .username("normal-user")
                .email("normal@example.com")
                .roles(Set.of(new Role(1L, RoleName.USER)))
                .enabled(true)
                .accountNonLocked(true)
                .build();
    }

    @Test
    void updateUserRoles_ShouldPreserveRoleUserAndAddOperator() {
        UpdateUserRolesRequest request = UpdateUserRolesRequest.builder()
                .roles(Set.of(RoleName.OPERATOR))
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(new Role(1L, RoleName.USER)));
        when(roleRepository.findByName(RoleName.OPERATOR)).thenReturn(Optional.of(new Role(2L, RoleName.OPERATOR)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSummaryResponse response = userAdminService.updateUserRoles(10L, request);

        assertTrue(response.getRoles().contains(RoleName.USER));
        assertTrue(response.getRoles().contains(RoleName.OPERATOR));
        verify(userRepository).save(any(User.class));
    }
}
