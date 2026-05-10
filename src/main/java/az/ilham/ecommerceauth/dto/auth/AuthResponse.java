package az.ilham.ecommerceauth.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private LocalDateTime accessTokenExpiresAt;
    private LocalDateTime refreshTokenExpiresAt;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private String message;
}
