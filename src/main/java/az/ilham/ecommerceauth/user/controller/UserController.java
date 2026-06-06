package az.ilham.ecommerceauth.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "İstifadəçi Profili", description = "İstifadəçi profilinin idarə edilməsi üçün endpoint-lər")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "Cari istifadəçinin profilini əldə et")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "İstifadəçi profili uğurla əldə edildi"),
            @ApiResponse(responseCode = "401", description = "Access token yoxdur, etibarsızdır və ya müddəti bitib"),
            @ApiResponse(responseCode = "403", description = "İstifadəçinin bu resursa giriş icazəsi yoxdur")
    })
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities(),
                "message", "This is a protected profile information"
        ));
    }
}
