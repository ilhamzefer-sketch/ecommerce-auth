CREATE TABLE shops (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) UNIQUE NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shops_owner_user FOREIGN KEY (owner_user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE shop_members (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    membership_role VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_members_shop FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_shop_members_shop_user UNIQUE (shop_id, user_id)
);

CREATE INDEX idx_shops_owner_user_id ON shops(owner_user_id);
CREATE INDEX idx_shop_members_user_id ON shop_members(user_id);
CREATE INDEX idx_shop_members_shop_id ON shop_members(shop_id);
