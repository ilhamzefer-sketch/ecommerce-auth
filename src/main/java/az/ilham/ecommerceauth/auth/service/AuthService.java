package az.ilham.ecommerceauth.auth.service;

import az.ilham.ecommerceauth.common.exception.UserAlreadyExistsException;
import az.ilham.ecommerceauth.dto.auth.AuthResponse;
import az.ilham.ecommerceauth.dto.auth.RegisterRequest;
import az.ilham.ecommerceauth.security.JwtService;
import az.ilham.ecommerceauth.user.entity.Role;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.RoleRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import az.ilham.ecommerceauth.dto.auth.LoginRequest;
import az.ilham.ecommerceauth.security.CustomUserDetailsService;
import az.ilham.ecommerceauth.auth.entity.EmailVerificationToken;
import az.ilham.ecommerceauth.auth.entity.PasswordResetToken;
import az.ilham.ecommerceauth.auth.entity.RefreshToken;
import az.ilham.ecommerceauth.auth.repository.EmailVerificationTokenRepository;
import az.ilham.ecommerceauth.auth.repository.PasswordResetTokenRepository;
import az.ilham.ecommerceauth.dto.auth.ForgotPasswordRequest;
import az.ilham.ecommerceauth.dto.auth.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(userRole))
                .enabled(true)
                .accountNonLocked(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Generate verification token (logic for Level 9)
        generateEmailVerificationToken(user);

        return AuthResponse.builder()
                .message("User registered successfully. Please check your email to verify your account.")
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            String hash = refreshTokenService.hashToken(token);
            
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(hash)
                    .expiresAt(LocalDateTime.now().plusHours(2)) // 2 hours expiry
                    .build();
            
            passwordResetTokenRepository.save(resetToken);
            
            // In a real app, send email here with the 'token' (plain text)
            System.out.println("Password reset token for " + user.getEmail() + ": " + token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String hash = refreshTokenService.hashToken(request.getToken());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired token"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getUsername());

        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        // Security best practice: logout from all devices on password change
        refreshTokenService.revokeAllUserTokens(user);
    }

    @Transactional
    public void verifyEmail(String tokenValue) {
        String hash = refreshTokenService.hashToken(tokenValue);
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired token"));

        if (verificationToken.isUsed() || verificationToken.isExpired()) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(verificationToken);
    }

    private void generateEmailVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        String hash = refreshTokenService.hashToken(token);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusDays(1)) // 24 hours expiry
                .build();

        emailVerificationTokenRepository.save(verificationToken);
        
        // In a real app, send email here with the 'token'
        System.out.println("Email verification token for " + user.getEmail() + ": " + token);
    }

    @Transactional
    public LoginResult login(LoginRequest request, String userAgent, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.getUsernameOrEmail());
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow();

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0); // reset on success
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(LocalDateTime.now().plusNanos(jwtService.getJwtExpiration() * 1000000))
                .message("Login successful")
                .build();

        return new LoginResult(response, refreshToken);
    }

    @Transactional
    public LoginResult refreshToken(String refreshTokenValue, String userAgent, String ipAddress) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);

        if (refreshToken == null || !refreshToken.isActive()) {
            // Potential reuse attack or invalid token
            if (refreshToken != null) {
                refreshTokenService.revokeAllUserTokens(refreshToken.getUser());
            }
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        User user = refreshToken.getUser();
        
        // Revoke current token (rotation)
        refreshTokenService.revokeToken(refreshToken);

        // Generate new tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshTokenValue = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .accessTokenExpiresAt(LocalDateTime.now().plusNanos(jwtService.getJwtExpiration() * 1000000))
                .message("Token refreshed successfully")
                .build();

        return new LoginResult(response, newRefreshTokenValue);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }
    }

    @Transactional
    public void logoutAll(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);
        if (refreshToken != null) {
            refreshTokenService.revokeAllUserTokens(refreshToken.getUser());
        }
    }

    public record LoginResult(AuthResponse response, String refreshToken) {}
}
