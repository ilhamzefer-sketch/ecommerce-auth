CREATE TABLE shop_invites (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    invited_by_user_id BIGINT NOT NULL,
    invited_email VARCHAR(100) NOT NULL,
    token VARCHAR(120) NOT NULL UNIQUE,
    membership_role VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_invites_shop FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_invites_invited_by_user FOREIGN KEY (invited_by_user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_shop_invites_shop_id ON shop_invites(shop_id);
CREATE INDEX idx_shop_invites_invited_email ON shop_invites(invited_email);
