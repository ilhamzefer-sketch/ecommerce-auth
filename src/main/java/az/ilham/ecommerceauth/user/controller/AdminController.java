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
@RequestMapping("/api/admin")
@Tag(name = "Admin İdarəetməsi", description = "Yalnız ROLE_ADMIN roluna sahib istifadəçilər üçün endpoint-lər")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @GetMapping("/dashboard")
    @Operation(summary = "Admin idarəetmə panelinin məlumatlarını əldə et")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin panelinin məlumatları uğurla əldə edildi"),
            @ApiResponse(responseCode = "401", description = "Access token yoxdur, etibarsızdır və ya müddəti bitib"),
            @ApiResponse(responseCode = "403", description = "İstifadəçinin ROLE_ADMIN rolu yoxdur")
    })
    public ResponseEntity<?> getAdminDashboard() {
        return ResponseEntity.ok(Map.of(
                "title", "Admin Dashboard",
                "message", "This information is only visible to Admins",
                "status", "Healthy"
        ));
    }
}
