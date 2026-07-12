package az.ilham.ecommerceauth.user.service;

import az.ilham.ecommerceauth.dto.user.AddShopMemberRequest;
import az.ilham.ecommerceauth.dto.user.AcceptShopInviteRequest;
import az.ilham.ecommerceauth.dto.user.CreateShopRequest;
import az.ilham.ecommerceauth.dto.user.CreateShopInviteRequest;
import az.ilham.ecommerceauth.dto.user.ShopMemberResponse;
import az.ilham.ecommerceauth.dto.user.ShopInviteResponse;
import az.ilham.ecommerceauth.dto.user.ShopResponse;
import az.ilham.ecommerceauth.dto.user.TransferShopOwnershipRequest;
import az.ilham.ecommerceauth.user.entity.Shop;
import az.ilham.ecommerceauth.user.entity.ShopInvite;
import az.ilham.ecommerceauth.user.entity.ShopInviteStatus;
import az.ilham.ecommerceauth.user.entity.ShopMember;
import az.ilham.ecommerceauth.user.entity.ShopMemberRole;
import az.ilham.ecommerceauth.user.entity.ShopStatus;
import az.ilham.ecommerceauth.user.entity.ShopType;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.ShopInviteRepository;
import az.ilham.ecommerceauth.user.repository.ShopMemberRepository;
import az.ilham.ecommerceauth.user.repository.ShopRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ShopMemberRepository shopMemberRepository;

    @Mock
    private ShopInviteRepository shopInviteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShopService shopService;

    private User owner;
    private User teammate;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .username("owner-user")
                .email("owner@example.com")
                .firstName("Owner")
                .lastName("User")
                .build();

        teammate = User.builder()
                .id(2L)
                .username("team-user")
                .email("team@example.com")
                .firstName("Team")
                .lastName("User")
                .build();
    }

    @Test
    void createShop_ShouldCreateOwnerMembership() {
        CreateShopRequest request = CreateShopRequest.builder()
                .name("My Shop")
                .slug("My Shop")
                .description("Demo shop")
                .type(ShopType.BUSINESS)
                .build();

        when(userRepository.findByUsername("owner-user")).thenReturn(Optional.of(owner));
        when(shopRepository.existsBySlug("my-shop")).thenReturn(false);
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId(100L);
            shop.setStatus(ShopStatus.ACTIVE);
            return shop;
        });
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShopResponse response = shopService.createShop(request, "owner-user");

        assertEquals("my-shop", response.getSlug());
        assertEquals(ShopMemberRole.OWNER, response.getCurrentUserRole());
        verify(shopMemberRepository).save(any(ShopMember.class));
    }

    @Test
    void addShopMember_ShouldAllowOwnerToAddManager() {
        Shop shop = Shop.builder()
                .id(100L)
                .name("My Shop")
                .slug("my-shop")
                .type(ShopType.BUSINESS)
                .status(ShopStatus.ACTIVE)
                .ownerUser(owner)
                .build();

        ShopMember ownerMembership = ShopMember.builder()
                .shop(shop)
                .user(owner)
                .membershipRole(ShopMemberRole.OWNER)
                .active(true)
                .build();

        AddShopMemberRequest request = AddShopMemberRequest.builder()
                .memberIdentifier("team-user")
                .membershipRole(ShopMemberRole.MANAGER)
                .build();

        when(userRepository.findByUsername("owner-user")).thenReturn(Optional.of(owner));
        when(shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(100L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(userRepository.findByUsername("team-user")).thenReturn(Optional.of(teammate));
        when(shopMemberRepository.findByShopIdAndUserId(100L, 2L)).thenReturn(Optional.empty());
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShopMemberResponse response = shopService.addShopMember(100L, request, "owner-user");

        assertEquals(ShopMemberRole.MANAGER, response.getMembershipRole());
        assertTrue(response.isActive());
    }

    @Test
    void createInvite_ShouldCreatePendingInvite() {
        Shop shop = Shop.builder()
                .id(100L)
                .name("My Shop")
                .slug("my-shop")
                .type(ShopType.BUSINESS)
                .status(ShopStatus.ACTIVE)
                .ownerUser(owner)
                .build();

        ShopMember ownerMembership = ShopMember.builder()
                .shop(shop)
                .user(owner)
                .membershipRole(ShopMemberRole.OWNER)
                .active(true)
                .build();

        CreateShopInviteRequest request = CreateShopInviteRequest.builder()
                .invitedEmail("invitee@example.com")
                .membershipRole(ShopMemberRole.MANAGER)
                .build();

        when(userRepository.findByUsername("owner-user")).thenReturn(Optional.of(owner));
        when(shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(100L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(shopInviteRepository.existsByShopIdAndInvitedEmailAndStatus(100L, "invitee@example.com", ShopInviteStatus.PENDING))
                .thenReturn(false);
        when(shopInviteRepository.save(any(ShopInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShopInviteResponse response = shopService.createInvite(100L, request, "owner-user");

        assertEquals("invitee@example.com", response.getInvitedEmail());
        assertEquals(ShopInviteStatus.PENDING, response.getStatus());
    }

    @Test
    void acceptInvite_ShouldActivateMembershipForMatchingEmail() {
        Shop shop = Shop.builder()
                .id(100L)
                .name("My Shop")
                .slug("my-shop")
                .type(ShopType.BUSINESS)
                .status(ShopStatus.ACTIVE)
                .ownerUser(owner)
                .build();

        ShopInvite invite = ShopInvite.builder()
                .shop(shop)
                .invitedByUser(owner)
                .invitedEmail("team@example.com")
                .token("token-123")
                .membershipRole(ShopMemberRole.STAFF)
                .status(ShopInviteStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        AcceptShopInviteRequest request = AcceptShopInviteRequest.builder()
                .token("token-123")
                .build();

        when(userRepository.findByUsername("team-user")).thenReturn(Optional.of(teammate));
        when(shopInviteRepository.findByToken("token-123")).thenReturn(Optional.of(invite));
        when(shopMemberRepository.findByShopIdAndUserId(100L, 2L)).thenReturn(Optional.empty());
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopInviteRepository.save(any(ShopInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopMemberRepository.findAllByShopIdAndActiveTrue(100L)).thenReturn(List.of(
                ShopMember.builder()
                        .shop(shop)
                        .user(teammate)
                        .membershipRole(ShopMemberRole.STAFF)
                        .active(true)
                        .build()
        ));

        ShopResponse response = shopService.acceptInvite(request, "team-user");

        assertEquals(ShopMemberRole.STAFF, response.getCurrentUserRole());
    }

    @Test
    void leaveShop_ShouldDeactivateNonOwnerMembership() {
        Shop shop = Shop.builder()
                .id(100L)
                .name("My Shop")
                .slug("my-shop")
                .type(ShopType.BUSINESS)
                .status(ShopStatus.ACTIVE)
                .ownerUser(owner)
                .build();

        ShopMember teammateMembership = ShopMember.builder()
                .shop(shop)
                .user(teammate)
                .membershipRole(ShopMemberRole.STAFF)
                .active(true)
                .build();

        when(userRepository.findByUsername("team-user")).thenReturn(Optional.of(teammate));
        when(shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(100L, 2L)).thenReturn(Optional.of(teammateMembership));
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> shopService.leaveShop(100L, "team-user"));
        verify(shopMemberRepository).save(any(ShopMember.class));
    }

    @Test
    void cancelInvite_ShouldMarkInviteCancelled() {
        Shop shop = Shop.builder()
                .id(100L)
                .name("My Shop")
                .slug("my-shop")
                .type(ShopType.BUSINESS)
                .status(ShopStatus.ACTIVE)
                .ownerUser(owner)
                .build();

        ShopMember ownerMembership = ShopMember.builder()
                .shop(shop)
                .user(owner)
                .membershipRole(ShopMemberRole.OWNER)
                .active(true)
                .build();

        ShopInvite invite = ShopInvite.builder()
                .id(77L)
                .shop(shop)
                .invitedByUser(owner)
                .invitedEmail("invitee@example.com")
                .token("abc")
                .membershipRole(ShopMemberRole.MANAGER)
                .status(ShopInviteStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(userRepository.findByUsername("owner-user")).thenReturn(Optional.of(owner));
        when(shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(100L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(shopInviteRepository.findById(77L)).thenReturn(Optional.of(invite));
        when(shopInviteRepository.save(any(ShopInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShopInviteResponse response = shopService.cancelInvite(100L, 77L, "owner-user");

        assertEquals(ShopInviteStatus.CANCELLED, response.getStatus());
    }

    @Test
    void resendInvite_ShouldGeneratePendingInvite() {
        Shop shop = Shop.builder()
                .id(100L)
                .name("My Shop")
                .slug("my-shop")
                .type(ShopType.BUSINESS)
                .status(ShopStatus.ACTIVE)
                .ownerUser(owner)
                .build();

        ShopMember ownerMembership = ShopMember.builder()
                .shop(shop)
                .user(owner)
                .membershipRole(ShopMemberRole.OWNER)
                .active(true)
                .build();

        ShopInvite invite = ShopInvite.builder()
                .id(78L)
                .shop(shop)
                .invitedByUser(owner)
                .invitedEmail("invitee@example.com")
                .token("old-token")
                .membershipRole(ShopMemberRole.MANAGER)
                .status(ShopInviteStatus.CANCELLED)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findByUsername("owner-user")).thenReturn(Optional.of(owner));
        when(shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(100L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(shopInviteRepository.findById(78L)).thenReturn(Optional.of(invite));
        when(shopInviteRepository.save(any(ShopInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShopInviteResponse response = shopService.resendInvite(100L, 78L, "owner-user");

        assertEquals(ShopInviteStatus.PENDING, response.getStatus());
        assertTrue(response.getToken() != null && !response.getToken().isBlank());
    }

    @Test
    void transferOwnership_ShouldPromoteTargetAndDemoteCurrentOwner() {
        Shop shop = Shop.builder()
                .id(100L)
                .name("My Shop")
                .slug("my-shop")
                .type(ShopType.BUSINESS)
                .status(ShopStatus.ACTIVE)
                .ownerUser(owner)
                .build();

        ShopMember ownerMembership = ShopMember.builder()
                .shop(shop)
                .user(owner)
                .membershipRole(ShopMemberRole.OWNER)
                .active(true)
                .build();

        ShopMember managerMembership = ShopMember.builder()
                .shop(shop)
                .user(teammate)
                .membershipRole(ShopMemberRole.MANAGER)
                .active(true)
                .build();

        TransferShopOwnershipRequest request = TransferShopOwnershipRequest.builder()
                .newOwnerUserId(2L)
                .build();

        when(userRepository.findByUsername("owner-user")).thenReturn(Optional.of(owner));
        when(shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(100L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(100L, 2L)).thenReturn(Optional.of(managerMembership));
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopMemberRepository.findAllByShopIdAndActiveTrue(100L)).thenReturn(List.of(ownerMembership, managerMembership));

        ShopResponse response = shopService.transferOwnership(100L, request, "owner-user");

        assertEquals(ShopMemberRole.OWNER, response.getCurrentUserRole());
        assertEquals("team-user", response.getOwnerUsername());
        assertEquals(ShopMemberRole.MANAGER, ownerMembership.getMembershipRole());
        assertEquals(ShopMemberRole.OWNER, managerMembership.getMembershipRole());
    }
}
