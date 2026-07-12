package az.ilham.ecommerceauth.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for accepting a shop invite.")
public class AcceptShopInviteRequest {

    @NotBlank(message = "Invite token is required")
    private String token;
}
