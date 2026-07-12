package az.ilham.ecommerceauth.dto.user;

import az.ilham.ecommerceauth.user.entity.ShopMemberRole;
import az.ilham.ecommerceauth.user.entity.ShopStatus;
import az.ilham.ecommerceauth.user.entity.ShopType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shop details returned to authenticated shop members.")
public class ShopResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private ShopType type;
    private ShopStatus status;
    private Long ownerUserId;
    private String ownerUsername;
    private ShopMemberRole currentUserRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ShopMemberResponse> members;
}
