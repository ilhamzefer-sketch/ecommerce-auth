package az.ilham.ecommerceauth.user.service;

import az.ilham.ecommerceauth.common.exception.ResourceNotFoundException;
import az.ilham.ecommerceauth.dto.user.AddShopMemberRequest;
import az.ilham.ecommerceauth.dto.user.AcceptShopInviteRequest;
import az.ilham.ecommerceauth.dto.user.CreateShopRequest;
import az.ilham.ecommerceauth.dto.user.CreateShopInviteRequest;
import az.ilham.ecommerceauth.dto.user.ShopMemberResponse;
import az.ilham.ecommerceauth.dto.user.ShopInviteResponse;
import az.ilham.ecommerceauth.dto.user.ShopResponse;
import az.ilham.ecommerceauth.dto.user.TransferShopOwnershipRequest;
import az.ilham.ecommerceauth.dto.user.UpdateShopMemberRoleRequest;
import az.ilham.ecommerceauth.user.entity.Shop;
import az.ilham.ecommerceauth.user.entity.ShopInvite;
import az.ilham.ecommerceauth.user.entity.ShopInviteStatus;
import az.ilham.ecommerceauth.user.entity.ShopMember;
import az.ilham.ecommerceauth.user.entity.ShopMemberRole;
import az.ilham.ecommerceauth.user.entity.ShopStatus;
import az.ilham.ecommerceauth.user.entity.User;
import az.ilham.ecommerceauth.user.repository.ShopInviteRepository;
import az.ilham.ecommerceauth.user.repository.ShopMemberRepository;
import az.ilham.ecommerceauth.user.repository.ShopRepository;
import az.ilham.ecommerceauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopMemberRepository shopMemberRepository;
    private final ShopInviteRepository shopInviteRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShopResponse createShop(CreateShopRequest request, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);

        String normalizedSlug = normalizeSlug(request.getSlug());
        if (shopRepository.existsBySlug(normalizedSlug)) {
            throw new IllegalArgumentException("Shop slug is already in use");
        }

        Shop shop = Shop.builder()
                .name(request.getName().trim())
                .slug(normalizedSlug)
                .description(request.getDescription() == null ? null : request.getDescription().trim())
                .type(request.getType())
                .status(ShopStatus.ACTIVE)
                .ownerUser(currentUser)
                .build();

        Shop savedShop = shopRepository.save(shop);

        ShopMember ownerMember = ShopMember.builder()
                .shop(savedShop)
                .user(currentUser)
                .membershipRole(ShopMemberRole.OWNER)
                .active(true)
                .build();
        shopMemberRepository.save(ownerMember);

        return toShopResponse(savedShop, ownerMember, List.of(ownerMember));
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> getMyShops(String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);

        return shopMemberRepository.findAllByUserIdAndActiveTrue(currentUser.getId()).stream()
                .sorted(Comparator.comparing(ShopMember::getCreatedAt).reversed())
                .map(member -> toShopResponse(member.getShop(), member, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ShopResponse getShop(Long shopId, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());
        List<ShopMember> members = shopMemberRepository.findAllByShopIdAndActiveTrue(shopId);
        return toShopResponse(currentMembership.getShop(), currentMembership, members);
    }

    @Transactional(readOnly = true)
    public List<ShopMemberResponse> getShopMembers(Long shopId, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        requireMembership(shopId, currentUser.getId());

        return shopMemberRepository.findAllByShopIdAndActiveTrue(shopId).stream()
                .sorted(Comparator.comparing(ShopMember::getCreatedAt))
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public ShopMemberResponse addShopMember(Long shopId, AddShopMemberRequest request, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());
        ensureCanManageMembers(currentMembership);

        User memberUser = findUser(request.getMemberIdentifier());
        if (memberUser.getId().equals(currentUser.getId()) && request.getMembershipRole() != ShopMemberRole.OWNER) {
            throw new IllegalArgumentException("Owner cannot downgrade themselves through add member flow");
        }

        ShopMember member = shopMemberRepository.findByShopIdAndUserId(shopId, memberUser.getId())
                .map(existing -> {
                    existing.setMembershipRole(request.getMembershipRole());
                    existing.setActive(true);
                    return existing;
                })
                .orElseGet(() -> ShopMember.builder()
                        .shop(currentMembership.getShop())
                        .user(memberUser)
                        .membershipRole(request.getMembershipRole())
                        .active(true)
                        .build());

        if (member.getMembershipRole() == ShopMemberRole.OWNER
                && !currentMembership.getMembershipRole().equals(ShopMemberRole.OWNER)) {
            throw new IllegalArgumentException("Only an owner can assign the OWNER membership role");
        }

        ShopMember savedMember = shopMemberRepository.save(member);
        return toMemberResponse(savedMember);
    }

    @Transactional
    public ShopMemberResponse updateShopMemberRole(
            Long shopId,
            Long memberUserId,
            UpdateShopMemberRoleRequest request,
            String usernameOrEmail
    ) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());
        ensureCanManageMembers(currentMembership);

        ShopMember member = shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(shopId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop member not found: " + memberUserId));

        if (member.getMembershipRole() == ShopMemberRole.OWNER && !currentMembership.getUser().getId().equals(memberUserId)) {
            throw new IllegalArgumentException("Non-owner members cannot modify the owner role");
        }
        if (request.getMembershipRole() == ShopMemberRole.OWNER
                && currentMembership.getMembershipRole() != ShopMemberRole.OWNER) {
            throw new IllegalArgumentException("Only an owner can assign the OWNER membership role");
        }

        member.setMembershipRole(request.getMembershipRole());
        ShopMember savedMember = shopMemberRepository.save(member);
        return toMemberResponse(savedMember);
    }

    @Transactional
    public void deactivateShopMember(Long shopId, Long memberUserId, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());
        ensureCanManageMembers(currentMembership);

        ShopMember member = shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(shopId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop member not found: " + memberUserId));

        if (member.getMembershipRole() == ShopMemberRole.OWNER) {
            throw new IllegalArgumentException("Owner membership cannot be deactivated");
        }
        if (member.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Use a dedicated leave flow to remove yourself from a shop");
        }

        member.setActive(false);
        shopMemberRepository.save(member);
    }

    @Transactional
    public void leaveShop(Long shopId, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember membership = requireMembership(shopId, currentUser.getId());

        if (membership.getMembershipRole() == ShopMemberRole.OWNER) {
            long activeMemberCount = shopMemberRepository.findAllByShopIdAndActiveTrue(shopId).size();
            if (activeMemberCount > 1) {
                throw new IllegalArgumentException("Owner must transfer ownership before leaving a shop with other active members");
            }
            throw new IllegalArgumentException("Owner cannot leave the shop without an ownership transfer flow");
        }

        membership.setActive(false);
        shopMemberRepository.save(membership);
    }

    @Transactional
    public ShopResponse transferOwnership(Long shopId, TransferShopOwnershipRequest request, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());

        if (currentMembership.getMembershipRole() != ShopMemberRole.OWNER) {
            throw new IllegalArgumentException("Only the current owner can transfer shop ownership");
        }
        if (currentUser.getId().equals(request.getNewOwnerUserId())) {
            throw new IllegalArgumentException("New owner must be different from the current owner");
        }

        ShopMember newOwnerMembership = shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(shopId, request.getNewOwnerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target shop member not found: " + request.getNewOwnerUserId()));

        Shop shop = currentMembership.getShop();
        shop.setOwnerUser(newOwnerMembership.getUser());
        shopRepository.save(shop);

        currentMembership.setMembershipRole(ShopMemberRole.MANAGER);
        newOwnerMembership.setMembershipRole(ShopMemberRole.OWNER);
        shopMemberRepository.save(currentMembership);
        ShopMember savedNewOwnerMembership = shopMemberRepository.save(newOwnerMembership);

        List<ShopMember> members = shopMemberRepository.findAllByShopIdAndActiveTrue(shopId);
        return toShopResponse(shop, savedNewOwnerMembership, members);
    }

    @Transactional
    public ShopInviteResponse createInvite(Long shopId, CreateShopInviteRequest request, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());
        ensureCanManageMembers(currentMembership);

        String normalizedEmail = request.getInvitedEmail().trim().toLowerCase();
        if (shopInviteRepository.existsByShopIdAndInvitedEmailAndStatus(shopId, normalizedEmail, ShopInviteStatus.PENDING)) {
            throw new IllegalArgumentException("An active invite already exists for this email");
        }
        if (request.getMembershipRole() == ShopMemberRole.OWNER
                && currentMembership.getMembershipRole() != ShopMemberRole.OWNER) {
            throw new IllegalArgumentException("Only an owner can invite another owner");
        }

        ShopInvite invite = ShopInvite.builder()
                .shop(currentMembership.getShop())
                .invitedByUser(currentUser)
                .invitedEmail(normalizedEmail)
                .token(UUID.randomUUID().toString())
                .membershipRole(request.getMembershipRole())
                .status(ShopInviteStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        ShopInvite savedInvite = shopInviteRepository.save(invite);
        return toInviteResponse(savedInvite);
    }

    @Transactional(readOnly = true)
    public List<ShopInviteResponse> getShopInvites(Long shopId, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        requireMembership(shopId, currentUser.getId());

        return shopInviteRepository.findAllByShopIdOrderByCreatedAtDesc(shopId).stream()
                .map(this::toInviteResponse)
                .toList();
    }

    @Transactional
    public ShopInviteResponse cancelInvite(Long shopId, Long inviteId, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());
        ensureCanManageMembers(currentMembership);

        ShopInvite invite = shopInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop invite not found: " + inviteId));

        if (!invite.getShop().getId().equals(shopId)) {
            throw new IllegalArgumentException("Invite does not belong to the specified shop");
        }
        if (invite.getStatus() != ShopInviteStatus.PENDING) {
            throw new IllegalArgumentException("Only pending invites can be cancelled");
        }

        invite.setStatus(ShopInviteStatus.CANCELLED);
        ShopInvite savedInvite = shopInviteRepository.save(invite);
        return toInviteResponse(savedInvite);
    }

    @Transactional
    public ShopInviteResponse resendInvite(Long shopId, Long inviteId, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);
        ShopMember currentMembership = requireMembership(shopId, currentUser.getId());
        ensureCanManageMembers(currentMembership);

        ShopInvite invite = shopInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop invite not found: " + inviteId));

        if (!invite.getShop().getId().equals(shopId)) {
            throw new IllegalArgumentException("Invite does not belong to the specified shop");
        }
        if (invite.getStatus() != ShopInviteStatus.PENDING
                && invite.getStatus() != ShopInviteStatus.EXPIRED
                && invite.getStatus() != ShopInviteStatus.CANCELLED) {
            throw new IllegalArgumentException("This invite cannot be resent");
        }
        if (invite.getMembershipRole() == ShopMemberRole.OWNER
                && currentMembership.getMembershipRole() != ShopMemberRole.OWNER) {
            throw new IllegalArgumentException("Only an owner can resend an owner invite");
        }

        invite.setToken(UUID.randomUUID().toString());
        invite.setStatus(ShopInviteStatus.PENDING);
        invite.setAcceptedAt(null);
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));

        ShopInvite savedInvite = shopInviteRepository.save(invite);
        return toInviteResponse(savedInvite);
    }

    @Transactional
    public ShopResponse acceptInvite(AcceptShopInviteRequest request, String usernameOrEmail) {
        User currentUser = findUser(usernameOrEmail);

        ShopInvite invite = shopInviteRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Shop invite not found"));

        if (invite.getStatus() != ShopInviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite is no longer active");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(ShopInviteStatus.EXPIRED);
            shopInviteRepository.save(invite);
            throw new IllegalArgumentException("Invite has expired");
        }
        if (!invite.getInvitedEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new IllegalArgumentException("Invite email does not match the current account");
        }

        ShopMember membership = shopMemberRepository.findByShopIdAndUserId(invite.getShop().getId(), currentUser.getId())
                .map(existing -> {
                    existing.setMembershipRole(invite.getMembershipRole());
                    existing.setActive(true);
                    return existing;
                })
                .orElseGet(() -> ShopMember.builder()
                        .shop(invite.getShop())
                        .user(currentUser)
                        .membershipRole(invite.getMembershipRole())
                        .active(true)
                        .build());

        ShopMember savedMembership = shopMemberRepository.save(membership);
        invite.setStatus(ShopInviteStatus.ACCEPTED);
        invite.setAcceptedAt(LocalDateTime.now());
        shopInviteRepository.save(invite);

        List<ShopMember> members = shopMemberRepository.findAllByShopIdAndActiveTrue(invite.getShop().getId());
        return toShopResponse(savedMembership.getShop(), savedMembership, members);
    }

    private User findUser(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOrEmail));
    }

    private ShopMember requireMembership(Long shopId, Long userId) {
        return shopMemberRepository.findByShopIdAndUserIdAndActiveTrue(shopId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop membership not found for this user"));
    }

    private void ensureCanManageMembers(ShopMember membership) {
        if (membership.getMembershipRole() != ShopMemberRole.OWNER
                && membership.getMembershipRole() != ShopMemberRole.MANAGER) {
            throw new IllegalArgumentException("Current user cannot manage shop members");
        }
    }

    private ShopResponse toShopResponse(Shop shop, ShopMember currentMembership, List<ShopMember> members) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .slug(shop.getSlug())
                .description(shop.getDescription())
                .type(shop.getType())
                .status(shop.getStatus())
                .ownerUserId(shop.getOwnerUser().getId())
                .ownerUsername(shop.getOwnerUser().getUsername())
                .currentUserRole(currentMembership.getMembershipRole())
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .members(members.stream().map(this::toMemberResponse).toList())
                .build();
    }

    private ShopMemberResponse toMemberResponse(ShopMember member) {
        return ShopMemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .firstName(member.getUser().getFirstName())
                .lastName(member.getUser().getLastName())
                .membershipRole(member.getMembershipRole())
                .active(member.isActive())
                .joinedAt(member.getCreatedAt())
                .build();
    }

    private ShopInviteResponse toInviteResponse(ShopInvite invite) {
        return ShopInviteResponse.builder()
                .id(invite.getId())
                .shopId(invite.getShop().getId())
                .shopName(invite.getShop().getName())
                .invitedEmail(invite.getInvitedEmail())
                .membershipRole(invite.getMembershipRole())
                .status(invite.getStatus())
                .token(invite.getToken())
                .expiresAt(invite.getExpiresAt())
                .acceptedAt(invite.getAcceptedAt())
                .createdAt(invite.getCreatedAt())
                .build();
    }

    private String normalizeSlug(String slug) {
        String normalized = slug.trim().toLowerCase()
                .replaceAll("[^a-z0-9-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        if (normalized.length() < 3) {
            throw new IllegalArgumentException("Shop slug must contain at least 3 valid characters");
        }

        return normalized;
    }
}
