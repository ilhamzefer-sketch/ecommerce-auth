package az.ilham.ecommerceauth.auth.controller;

import az.ilham.ecommerceauth.auth.service.AuthService;
import az.ilham.ecommerceauth.auth.service.RefreshTokenService;
import az.ilham.ecommerceauth.dto.auth.AuthResponse;
import az.ilham.ecommerceauth.dto.auth.RegisterRequest;
import az.ilham.ecommerceauth.dto.auth.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;
import az.ilham.ecommerceauth.dto.auth.ForgotPasswordRequest;
import az.ilham.ecommerceauth.dto.auth.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("${application.security.jwt.refresh-token.cookie.name}")
    private String cookieName;

    @Value("${application.security.jwt.refresh-token.cookie.max-age}")
    private int cookieMaxAge;

    @Value("${application.security.jwt.refresh-token.cookie.secure}")
    private boolean cookieSecure;

    @Value("${application.security.jwt.refresh-token.cookie.http-only}")
    private boolean cookieHttpOnly;

    @Value("${application.security.jwt.refresh-token.cookie.same-site}")
    private String cookieSameSite;

    @Value("${application.security.jwt.refresh-token.cookie.path}")
    private String cookiePath;

    @GetMapping("/status")
    @Operation(summary = "Check auth service status")
    public String status() {
        return "Auth service is up";
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive tokens")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String ipAddress = request.getRemoteAddr();

        AuthService.LoginResult result = authService.login(loginRequest, userAgent, ipAddress);

        ResponseCookie refreshTokenCookie = refreshTokenService.createRefreshTokenResponseCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(result.response());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String ipAddress = request.getRemoteAddr();

        AuthService.LoginResult result = authService.refreshToken(refreshToken, userAgent, ipAddress);

        ResponseCookie refreshTokenCookie = refreshTokenService.createRefreshTokenResponseCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from current session")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        authService.logout(refreshToken);
        
        ResponseCookie cookie = refreshTokenService.deleteRefreshTokenCookie();
        
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all sessions")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        authService.logoutAll(refreshToken);

        ResponseCookie cookie = refreshTokenService.deleteRefreshTokenCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset link")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(AuthResponse.builder()
                .message("If your email is registered, you will receive a reset link.")
                .build());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Password reset successfully. You can now login with your new password.")
                .build());
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email using token")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Email verified successfully.")
                .build());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new BadCredentialsException("Refresh token cookie is missing");
        }
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new BadCredentialsException("Refresh token cookie is missing");
    }
}
