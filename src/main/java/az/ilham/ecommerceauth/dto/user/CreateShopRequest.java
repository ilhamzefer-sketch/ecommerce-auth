package az.ilham.ecommerceauth.dto.user;

import az.ilham.ecommerceauth.user.entity.ShopType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a shop.")
public class CreateShopRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 120, message = "Shop name must be at most 120 characters")
    private String name;

    @NotBlank(message = "Shop slug is required")
    @Size(min = 3, max = 140, message = "Shop slug must be between 3 and 140 characters")
    private String slug;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotNull(message = "Shop type is required")
    private ShopType type;
}
