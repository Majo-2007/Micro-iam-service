ALTER TABLE identity.user
    DROP COLUMN must_change_password,
    DROP COLUMN temp_password_expires_at;
