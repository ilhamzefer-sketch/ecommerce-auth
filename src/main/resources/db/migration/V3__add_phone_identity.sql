ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
ALTER TABLE users ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users
SET phone_number = CONCAT('+994000', LPAD(CAST(id AS VARCHAR), 6, '0'))
WHERE phone_number IS NULL;

ALTER TABLE users ALTER COLUMN phone_number SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_phone_number UNIQUE (phone_number);
CREATE INDEX idx_users_phone_number ON users(phone_number);
