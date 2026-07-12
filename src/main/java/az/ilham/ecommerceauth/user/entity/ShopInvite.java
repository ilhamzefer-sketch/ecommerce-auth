package az.ilham.ecommerceauth.user.entity;

import az.ilham.ecommerceauth.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopInvite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by_user_id", nullable = false)
    private User invitedByUser;

    @Column(nullable = false, length = 100)
    private String invitedEmail;

    @Column(nullable = false, unique = true, length = 120)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_role", nullable = false, length = 40)
    private ShopMemberRole membershipRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopInviteStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime acceptedAt;
}
