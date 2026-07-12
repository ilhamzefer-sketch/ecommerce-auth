package az.ilham.ecommerceauth.dto.user;

import az.ilham.ecommerceauth.user.entity.ShopMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for adding a user to a shop.")
public class AddShopMemberRequest {

    @NotBlank(message = "Member identifier is required")
    @Schema(description = "Username or email of the user to add.", example = "demo-user")
    private String memberIdentifier;

    @NotNull(message = "Membership role is required")
    private ShopMemberRole membershipRole;
}
