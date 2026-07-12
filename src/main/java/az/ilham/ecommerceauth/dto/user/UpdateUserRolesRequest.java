package az.ilham.ecommerceauth.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a user's system roles.")
public class UpdateUserRolesRequest {

    @NotEmpty(message = "At least one role must be provided")
    @Schema(
            description = "Role names to assign. ROLE_USER is always preserved automatically.",
            example = "[\"ROLE_USER\", \"ROLE_OPERATOR\"]"
    )
    private Set<String> roles;
}
