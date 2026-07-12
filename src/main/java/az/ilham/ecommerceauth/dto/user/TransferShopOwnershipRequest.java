package az.ilham.ecommerceauth.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for transferring shop ownership to another active member.")
public class TransferShopOwnershipRequest {

    @NotNull(message = "New owner user id is required")
    private Long newOwnerUserId;
}
