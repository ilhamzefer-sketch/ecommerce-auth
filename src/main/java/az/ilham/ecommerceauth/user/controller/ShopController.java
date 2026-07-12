package az.ilham.ecommerceauth.user.controller;

import az.ilham.ecommerceauth.dto.user.AddShopMemberRequest;
import az.ilham.ecommerceauth.dto.user.AcceptShopInviteRequest;
import az.ilham.ecommerceauth.dto.user.CreateShopRequest;
import az.ilham.ecommerceauth.dto.user.CreateShopInviteRequest;
import az.ilham.ecommerceauth.dto.user.ShopMemberResponse;
import az.ilham.ecommerceauth.dto.user.ShopInviteResponse;
import az.ilham.ecommerceauth.dto.user.ShopResponse;
import az.ilham.ecommerceauth.dto.user.TransferShopOwnershipRequest;
import az.ilham.ecommerceauth.dto.user.UpdateShopMemberRoleRequest;
import az.ilham.ecommerceauth.user.service.ShopService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@Tag(name = "Shop Membership", description = "Shop creation and team membership endpoints for authenticated users.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @Operation(summary = "Create a new shop and assign the current user as OWNER")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop created successfully",
                    content = @Content(schema = @Schema(implementation = ShopResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or duplicate slug"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired")
    })
    public ResponseEntity<ShopResponse> createShop(
            @Valid @RequestBody CreateShopRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.createShop(request, userDetails.getUsername()));
    }

    @GetMapping("/mine")
    @Operation(summary = "List shops where the current user is a member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop list returned successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShopResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired")
    })
    public ResponseEntity<List<ShopResponse>> getMyShops(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(shopService.getMyShops(userDetails.getUsername()));
    }

    @GetMapping("/{shopId}")
    @Operation(summary = "Get a shop visible to the current member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop returned successfully",
                    content = @Content(schema = @Schema(implementation = ShopResponse.class))),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop or membership not found")
    })
    public ResponseEntity<ShopResponse> getShop(
            @PathVariable Long shopId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.getShop(shopId, userDetails.getUsername()));
    }

    @GetMapping("/{shopId}/members")
    @Operation(summary = "Get members of a shop where the current user belongs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop members returned successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShopMemberResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop or membership not found")
    })
    public ResponseEntity<List<ShopMemberResponse>> getShopMembers(
            @PathVariable Long shopId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.getShopMembers(shopId, userDetails.getUsername()));
    }

    @PostMapping("/{shopId}/members")
    @Operation(summary = "Add or reactivate a shop member by username or email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop member saved successfully",
                    content = @Content(schema = @Schema(implementation = ShopMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Current user cannot manage members or payload is invalid"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop, membership, or target user not found")
    })
    public ResponseEntity<ShopMemberResponse> addShopMember(
            @PathVariable Long shopId,
            @Valid @RequestBody AddShopMemberRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.addShopMember(shopId, request, userDetails.getUsername()));
    }

    @PatchMapping("/{shopId}/members/{memberUserId}")
    @Operation(summary = "Update a shop member role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop member role updated successfully",
                    content = @Content(schema = @Schema(implementation = ShopMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Current user cannot manage members or payload is invalid"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop, membership, or target user not found")
    })
    public ResponseEntity<ShopMemberResponse> updateMemberRole(
            @PathVariable Long shopId,
            @PathVariable Long memberUserId,
            @Valid @RequestBody UpdateShopMemberRoleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.updateShopMemberRole(
                shopId,
                memberUserId,
                request,
                userDetails.getUsername()
        ));
    }

    @DeleteMapping("/{shopId}/members/{memberUserId}")
    @Operation(summary = "Deactivate a shop member")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Shop member deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Current user cannot manage members or target cannot be deactivated"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop, membership, or target user not found")
    })
    public ResponseEntity<Void> deactivateMember(
            @PathVariable Long shopId,
            @PathVariable Long memberUserId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        shopService.deactivateShopMember(shopId, memberUserId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{shopId}/invites")
    @Operation(summary = "Create a shop invite for an email address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop invite created successfully",
                    content = @Content(schema = @Schema(implementation = ShopInviteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Current user cannot manage invites or payload is invalid"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop or membership not found")
    })
    public ResponseEntity<ShopInviteResponse> createInvite(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateShopInviteRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.createInvite(shopId, request, userDetails.getUsername()));
    }

    @GetMapping("/{shopId}/invites")
    @Operation(summary = "List invites for a shop where the current user belongs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop invites returned successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShopInviteResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop or membership not found")
    })
    public ResponseEntity<List<ShopInviteResponse>> getInvites(
            @PathVariable Long shopId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.getShopInvites(shopId, userDetails.getUsername()));
    }

    @PostMapping("/invites/accept")
    @Operation(summary = "Accept a shop invite with a token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shop invite accepted successfully",
                    content = @Content(schema = @Schema(implementation = ShopResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invite is invalid, expired, or email does not match"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Invite or user not found")
    })
    public ResponseEntity<ShopResponse> acceptInvite(
            @Valid @RequestBody AcceptShopInviteRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.acceptInvite(request, userDetails.getUsername()));
    }

    @DeleteMapping("/{shopId}/members/me")
    @Operation(summary = "Leave a shop as the current authenticated member")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Current user left the shop successfully"),
            @ApiResponse(responseCode = "400", description = "Owner cannot leave without ownership transfer"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop or membership not found")
    })
    public ResponseEntity<Void> leaveShop(
            @PathVariable Long shopId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        shopService.leaveShop(shopId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{shopId}/invites/{inviteId}/cancel")
    @Operation(summary = "Cancel a pending shop invite")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invite cancelled successfully",
                    content = @Content(schema = @Schema(implementation = ShopInviteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invite cannot be cancelled or does not belong to the shop"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop, membership, or invite not found")
    })
    public ResponseEntity<ShopInviteResponse> cancelInvite(
            @PathVariable Long shopId,
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.cancelInvite(shopId, inviteId, userDetails.getUsername()));
    }

    @PatchMapping("/{shopId}/invites/{inviteId}/resend")
    @Operation(summary = "Resend a shop invite by generating a new token and expiry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invite resent successfully",
                    content = @Content(schema = @Schema(implementation = ShopInviteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invite cannot be resent or does not belong to the shop"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop, membership, or invite not found")
    })
    public ResponseEntity<ShopInviteResponse> resendInvite(
            @PathVariable Long shopId,
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.resendInvite(shopId, inviteId, userDetails.getUsername()));
    }

    @PatchMapping("/{shopId}/ownership-transfer")
    @Operation(summary = "Transfer shop ownership to another active member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully",
                    content = @Content(schema = @Schema(implementation = ShopResponse.class))),
            @ApiResponse(responseCode = "400", description = "Only the current owner can transfer ownership or target is invalid"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "404", description = "Shop, membership, or target member not found")
    })
    public ResponseEntity<ShopResponse> transferOwnership(
            @PathVariable Long shopId,
            @Valid @RequestBody TransferShopOwnershipRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.transferOwnership(shopId, request, userDetails.getUsername()));
    }
}
