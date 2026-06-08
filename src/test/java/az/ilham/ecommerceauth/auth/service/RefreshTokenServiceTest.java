package az.ilham.ecommerceauth.auth.service;

import az.ilham.ecommerceauth.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RefreshTokenServiceTest {

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(mock(RefreshTokenRepository.class));
        ReflectionTestUtils.setField(refreshTokenService, "cookieName", "refresh_token");
        ReflectionTestUtils.setField(refreshTokenService, "cookieMaxAge", 604800);
        ReflectionTestUtils.setField(refreshTokenService, "cookieSecure", true);
        ReflectionTestUtils.setField(refreshTokenService, "cookieHttpOnly", true);
        ReflectionTestUtils.setField(refreshTokenService, "cookieSameSite", "Strict");
        ReflectionTestUtils.setField(refreshTokenService, "cookiePath", "/api/auth");
    }

    @Test
    void createRefreshTokenResponseCookie_ShouldCoverRefreshAndLogoutEndpoints() {
        ResponseCookie cookie = refreshTokenService.createRefreshTokenResponseCookie("token-value");

        assertEquals("/api/auth", cookie.getPath());
        assertEquals(604800, cookie.getMaxAge().getSeconds());
    }

    @Test
    void deleteRefreshTokenCookie_ShouldUseSamePathAndExpireImmediately() {
        ResponseCookie cookie = refreshTokenService.deleteRefreshTokenCookie();

        assertEquals("/api/auth", cookie.getPath());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }
}
