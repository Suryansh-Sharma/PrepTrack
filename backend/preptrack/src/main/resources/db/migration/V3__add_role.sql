-- ============================================================
-- Add role to app_user
-- ============================================================

ALTER TABLE app_user
    ADD COLUMN role VARCHAR(30);

-- Existing User
UPDATE app_user
SET role = 'USER'
WHERE role is NULL;

-- Future User
ALTER TABLE app_user
    ALTER COLUMN role SET DEFAULT 'USER';

-- Only valid roles are allowed
ALTER TABLE app_user
    ADD CONSTRAINT chk_app_user_roles
    CHECK ( role in ('USER','ADMIN','MANAGER'));