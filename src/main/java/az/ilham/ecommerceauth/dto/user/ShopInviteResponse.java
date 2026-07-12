package az.ilham.ecommerceauth.dto.user;

import az.ilham.ecommerceauth.user.entity.ShopInviteStatus;
import az.ilham.ecommerceauth.user.entity.ShopMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shop invite details for frontend invitation management.")
public class ShopInviteResponse {

    private Long id;
    private Long shopId;
    private String shopName;
    private String invitedEmail;
    private ShopMemberRole membershipRole;
    private ShopInviteStatus status;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
}
