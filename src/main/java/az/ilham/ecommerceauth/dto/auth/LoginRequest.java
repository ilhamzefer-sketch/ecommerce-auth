package az.ilham.ecommerceauth.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username, email or phone number is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
