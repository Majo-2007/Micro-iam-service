ALTER TABLE identity.user
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN temp_password_expires_at TIMESTAMPTZ NULL;
