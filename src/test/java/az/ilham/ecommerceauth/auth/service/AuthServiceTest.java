package az.ilham.ecommerceauth.auth.service;

import az.ilham.ecommerceauth.auth.repository.EmailVerificationTokenRepository;
import az.ilham.ecommerceauth.auth.repository.PasswordResetTokenRepository;
import az.ilham.ecommerceauth.common.exception.UserAlreadyExistsException;
import az.ilham.ecommerceauth.dto.auth.AuthResponse;
import az.ilham.ecommerceauth.dto.auth.LoginRequest;
import az.ilham.ecommerceauth.dto.auth.RegisterRequest;
import az.ilham.ecommerceauth.security.CustomUserDetailsService;
import az.ilham.ecommerceauth.security.JwtService;
import az.ilham.ecommerceauth.security.SecurityUserPrincipal;
import az.ilham.ecommerceauth.user.entity.Role;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.RoleRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private CustomUserDetailsService userDetailsService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock
    private PhoneNumberNormalizer phoneNumberNormalizer;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private Role userRole;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("+994501112233")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
        userRole = new Role(1L, "ROLE_USER");
    }

    @Test
    void registrationCreatesOneUnifiedUserAccount() {
        when(phoneNumberNormalizer.normalize(any())).thenReturn("+994501112233");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        AuthResponse response = authService.register(registerRequest);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertNotNull(response);
        assertEquals(Set.of(userRole), savedUser.getRoles());
        assertEquals("+994501112233", savedUser.getPhoneNumber());
        assertEquals("User registered successfully. Please check your email to verify your account.", response.getMessage());
    }

    @Test
    void duplicatePhoneNumberIsRejected() {
        when(phoneNumberNormalizer.normalize(any())).thenReturn("+994501112233");
        when(userRepository.existsByPhoneNumber("+994501112233")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
    }

    @Test
    void loginAcceptsPhoneNumberAsTheSingleIdentifier() {
        User user = User.builder()
                .id(7L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("+994501112233")
                .roles(Set.of(userRole))
                .build();
        SecurityUserPrincipal principal = new SecurityUserPrincipal(
                7L,
                "testuser",
                "hashed_password",
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(phoneNumberNormalizer.normalize("+994 50 111 22 33")).thenReturn("+994501112233");
        when(userRepository.findByUsernameIgnoreCase("+994 50 111 22 33")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("+994 50 111 22 33")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("+994501112233")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(principal);
        when(jwtService.generateToken(principal)).thenReturn("access-token");
        when(jwtService.getJwtExpiration()).thenReturn(900000L);
        when(refreshTokenService.createRefreshToken(user, "browser", "127.0.0.1"))
                .thenReturn("refresh-token");

        AuthLoginResult result = authService.login(
                new LoginRequest("+994 50 111 22 33", "password123"),
                "browser",
                "127.0.0.1"
        );

        assertEquals("access-token", result.response().getAccessToken());
        assertEquals("refresh-token", result.refreshToken());
    }
}
