package az.ilham.ecommerceauth.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/operator")
@Tag(name = "Operator Workspace", description = "Operator, moderator, and admin access examples for back-office UI.")
@SecurityRequirement(name = "bearerAuth")
public class OperatorController {

    @GetMapping("/workspace")
    @Operation(summary = "Get sample operator workspace data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operator workspace data returned successfully"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "The user does not have an operator-level role")
    })
    public ResponseEntity<Map<String, Object>> getWorkspace() {
        return ResponseEntity.ok(Map.of(
                "queueName", "listing-review",
                "allowedRoles", new String[]{"ROLE_OPERATOR", "ROLE_MODERATOR", "ROLE_ADMIN"},
                "message", "This area is available to operator-level accounts."
        ));
    }
}
