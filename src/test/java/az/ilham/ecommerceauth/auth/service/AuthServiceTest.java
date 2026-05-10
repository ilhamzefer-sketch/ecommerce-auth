package az.ilham.ecommerceauth.auth.service;

import az.ilham.ecommerceauth.auth.repository.EmailVerificationTokenRepository;
import az.ilham.ecommerceauth.auth.repository.PasswordResetTokenRepository;
import az.ilham.ecommerceauth.dto.auth.AuthResponse;
import az.ilham.ecommerceauth.dto.auth.RegisterRequest;
import az.ilham.ecommerceauth.security.JwtService;
import az.ilham.ecommerceauth.user.entity.Role;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.RoleRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void register_ShouldSaveUserAndReturnSuccessMessage() {
        // Arrange
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1L, "ROLE_USER")));
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("User registered successfully. Please check your email to verify your account.", response.getMessage());
        verify(userRepository).save(any(User.class));
    }
}
