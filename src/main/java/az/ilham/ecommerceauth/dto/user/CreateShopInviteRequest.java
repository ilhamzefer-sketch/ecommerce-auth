package az.ilham.ecommerceauth.dto.user;

import az.ilham.ecommerceauth.user.entity.ShopMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request payload for creating a shop invitation.")
public class CreateShopInviteRequest {

    @NotBlank(message = "Invited email is required")
    @Email(message = "Invited email must be valid")
    private String invitedEmail;

    @NotNull(message = "Membership role is required")
    private ShopMemberRole membershipRole;
}
