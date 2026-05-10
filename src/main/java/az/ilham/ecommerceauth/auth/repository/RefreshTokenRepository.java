package az.ilham.ecommerceauth.auth.repository;

import az.ilham.ecommerceauth.auth.entity.RefreshToken;
import az.ilham.ecommerceauth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);
    void deleteAllByUser(User user);
}
