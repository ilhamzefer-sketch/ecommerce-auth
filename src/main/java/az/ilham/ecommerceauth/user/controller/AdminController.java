package az.ilham.ecommerceauth.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "Endpoints restricted to users with ROLE_ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard info")
    public ResponseEntity<?> getAdminDashboard() {
        return ResponseEntity.ok(Map.of(
                "title", "Admin Dashboard",
                "message", "This information is only visible to Admins",
                "status", "Healthy"
        ));
    }
}
