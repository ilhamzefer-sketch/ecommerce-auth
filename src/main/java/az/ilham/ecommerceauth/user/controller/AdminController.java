package az.ilham.ecommerceauth.user.controller;

import az.ilham.ecommerceauth.dto.user.UpdateUserRolesRequest;
import az.ilham.ecommerceauth.dto.user.UserSummaryResponse;
import az.ilham.ecommerceauth.user.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "Admin-only endpoints for system user and role management.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminController {

    private final UserAdminService userAdminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get basic admin dashboard data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin dashboard data returned successfully"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "The user does not have ROLE_ADMIN")
    })
    public ResponseEntity<Map<String, String>> getAdminDashboard() {
        return ResponseEntity.ok(Map.of(
                "title", "Admin Dashboard",
                "message", "This information is only visible to admins",
                "status", "Healthy"
        ));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users with their current system roles")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users returned successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserSummaryResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "The user does not have ROLE_ADMIN")
    })
    public ResponseEntity<List<UserSummaryResponse>> getUsers() {
        return ResponseEntity.ok(userAdminService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a single user with current system roles")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User returned successfully",
                    content = @Content(schema = @Schema(implementation = UserSummaryResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "The user does not have ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserSummaryResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userAdminService.getUserById(userId));
    }

    @PatchMapping("/users/{userId}/roles")
    @Operation(summary = "Assign system roles to a user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User roles updated successfully",
                    content = @Content(schema = @Schema(implementation = UserSummaryResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Unknown role name or invalid request"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "The user does not have ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserSummaryResponse> updateRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ResponseEntity.ok(userAdminService.updateUserRoles(userId, request));
    }
}
