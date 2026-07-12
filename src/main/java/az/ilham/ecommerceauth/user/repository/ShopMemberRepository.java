package az.ilham.ecommerceauth.user.repository;

import az.ilham.ecommerceauth.user.entity.ShopMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopMemberRepository extends JpaRepository<ShopMember, Long> {
    List<ShopMember> findAllByUserIdAndActiveTrue(Long userId);
    List<ShopMember> findAllByShopIdAndActiveTrue(Long shopId);
    Optional<ShopMember> findByShopIdAndUserIdAndActiveTrue(Long shopId, Long userId);
    Optional<ShopMember> findByShopIdAndUserId(Long shopId, Long userId);
}
