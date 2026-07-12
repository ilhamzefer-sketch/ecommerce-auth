package az.ilham.ecommerceauth.dto.user;

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
@Schema(description = "Shop member information for frontend team management.")
public class ShopMemberResponse {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private ShopMemberRole membershipRole;
    private boolean active;
    private LocalDateTime joinedAt;
}
