package az.ilham.ecommerceauth.user.repository;

import az.ilham.ecommerceauth.user.entity.ShopInvite;
import az.ilham.ecommerceauth.user.entity.ShopInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopInviteRepository extends JpaRepository<ShopInvite, Long> {
    List<ShopInvite> findAllByShopIdOrderByCreatedAtDesc(Long shopId);
    Optional<ShopInvite> findByToken(String token);
    boolean existsByShopIdAndInvitedEmailAndStatus(Long shopId, String invitedEmail, ShopInviteStatus status);
}
