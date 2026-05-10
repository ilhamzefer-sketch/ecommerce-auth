package az.ilham.ecommerceauth.auth.service;

import az.ilham.ecommerceauth.auth.entity.RefreshToken;
import az.ilham.ecommerceauth.auth.repository.RefreshTokenRepository;
import az.ilham.ecommerceauth.user.entity.User;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

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

    @Transactional
    public String createRefreshToken(User user, String userAgent, String ipAddress) {
        String tokenValue = generateRandomToken();
        String tokenHash = hashToken(tokenValue);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1000000))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    public Cookie createRefreshTokenCookie(String tokenValue) {
        Cookie cookie = new Cookie(cookieName, tokenValue);
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge(cookieMaxAge);
        cookie.setPath(cookiePath);
        return cookie;
    }

    public ResponseCookie createRefreshTokenResponseCookie(String tokenValue) {
        return ResponseCookie.from(cookieName, tokenValue)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(cookieMaxAge)
                .sameSite(cookieSameSite)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    public RefreshToken findByToken(String tokenValue) {
        String hash = hashToken(tokenValue);
        return refreshTokenRepository.findByTokenHash(hash).orElse(null);
    }

    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    public String generateRandomToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
