-- ============================================================
-- Flyway Migration
-- V1__create_auth_tables.sql
-- Spring Boot / PostgreSQL
-- ============================================================
CREATE SEQUENCE app_user_seq
    START WITH 1
    INCREMENT BY 50;


CREATE TABLE app_user
(
    id                    INTEGER                  NOT NULL DEFAULT nextval('app_user_seq'),

    -- Identity
    email                 VARCHAR(255)             NOT NULL,
    contact               VARCHAR(255),
    password_hash         VARCHAR(255)             NOT NULL,
    display_name          VARCHAR(255),

    -- User preferences
    timezone              VARCHAR(100),

    -- Email verification
    email_verified_at     TIMESTAMP WITH TIME ZONE,

    -- Account / subscription
    plan                  VARCHAR(20)              NOT NULL DEFAULT 'FREE',
    status                VARCHAR(20)              NOT NULL DEFAULT 'ACTIVE',

    -- Account lifecycle
    deleted_at            TIMESTAMP WITH TIME ZONE,

    -- Login security
    failed_login_attempts INTEGER                  NOT NULL DEFAULT 0,
    locked_until          TIMESTAMP WITH TIME ZONE,

    -- Password lifecycle
    password_changed_at   TIMESTAMP WITH TIME ZONE,

    -- Audit timestamps
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_app_user
        PRIMARY KEY (id),

    CONSTRAINT uk_app_user_email
        UNIQUE (email),

    CONSTRAINT chk_app_user_failed_attempts
        CHECK (failed_login_attempts >= 0),

    CONSTRAINT chk_app_user_plan
        CHECK (
            plan IN (
                'FREE',
                'PRO',
                'PREMIUM'
            )
        ),

    CONSTRAINT chk_app_user_status
        CHECK (
            status IN (
                'PENDING_VERIFICATION',
                'ACTIVE',
                'LOCKED',
                'DISABLED',
                'SUSPENDED',
                'DELETED'
            )
        )
);


ALTER SEQUENCE app_user_seq
    OWNED BY app_user.id;


CREATE INDEX idx_app_user_status
    ON app_user (status);

CREATE INDEX idx_app_user_locked_until
    ON app_user (locked_until);


-- ============================================================
-- REFRESH TOKENS / USER SESSIONS
-- ============================================================

CREATE TABLE refresh_token
(
    id                   VARCHAR(255)             NOT NULL,

    user_id              INTEGER                  NOT NULL,

    -- Token lifecycle
    expires_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at         TIMESTAMP WITH TIME ZONE,
    revoked_at           TIMESTAMP WITH TIME ZONE,

    -- Session information
    device_info          VARCHAR(500),
    ip_address           VARCHAR(45),

    CONSTRAINT pk_refresh_token
        PRIMARY KEY (id),

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE
);


CREATE INDEX idx_refresh_token_user_id
    ON refresh_token (user_id);

CREATE INDEX idx_refresh_token_expires_at
    ON refresh_token (expires_at);

CREATE INDEX idx_refresh_token_revoked_at
    ON refresh_token (revoked_at);

CREATE INDEX idx_refresh_token_user_active
    ON refresh_token (user_id, expires_at)
    WHERE revoked_at IS NULL;


-- ============================================================
-- EMAIL VERIFICATION TOKENS
-- ============================================================

CREATE TABLE email_verification_token
(
    id         VARCHAR(255)             NOT NULL,

    user_id    INTEGER                  NOT NULL,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at    TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_email_verification_token
        PRIMARY KEY (id),

    CONSTRAINT fk_email_verification_token_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE
);


CREATE INDEX idx_email_verification_token_user_id
    ON email_verification_token (user_id);

CREATE INDEX idx_email_verification_token_expires_at
    ON email_verification_token (expires_at);

CREATE INDEX idx_email_verification_token_user_active
    ON email_verification_token (user_id, expires_at)
    WHERE used_at IS NULL;


-- ============================================================
-- PASSWORD RESET TOKENS
-- ============================================================

CREATE TABLE password_reset_token
(
    id         VARCHAR(255)             NOT NULL,

    user_id    INTEGER                  NOT NULL,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at    TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_password_reset_token
        PRIMARY KEY (id),

    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE
);


CREATE INDEX idx_password_reset_token_user_id
    ON password_reset_token (user_id);

CREATE INDEX idx_password_reset_token_expires_at
    ON password_reset_token (expires_at);

CREATE INDEX idx_password_reset_token_user_active
    ON password_reset_token (user_id, expires_at)
    WHERE used_at IS NULL;


-- ============================================================
-- SECURITY / LOGIN AUDIT
-- ============================================================

CREATE TABLE auth_login_attempt
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,

    user_id      INTEGER,

    email        VARCHAR(255)             NOT NULL,

    successful   BOOLEAN                  NOT NULL,

    ip_address   VARCHAR(45),

    user_agent   VARCHAR(1000),

    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_auth_login_attempt
        PRIMARY KEY (id),

    CONSTRAINT fk_auth_login_attempt_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE SET NULL
);


CREATE INDEX idx_auth_login_attempt_user_id
    ON auth_login_attempt (user_id);

CREATE INDEX idx_auth_login_attempt_email
    ON auth_login_attempt (email);

CREATE INDEX idx_auth_login_attempt_attempted_at
    ON auth_login_attempt (attempted_at);

CREATE INDEX idx_auth_login_attempt_email_attempted_at
    ON auth_login_attempt (email, attempted_at);


-- ============================================================
-- PASSWORD HISTORY
-- ============================================================

-- Stores previous password hashes.
-- Never store plaintext passwords.

CREATE TABLE password_history
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,

    user_id       INTEGER                  NOT NULL,

    password_hash VARCHAR(255)             NOT NULL,

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_password_history
        PRIMARY KEY (id),

    CONSTRAINT fk_password_history_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE
);


CREATE INDEX idx_password_history_user_id
    ON password_history (user_id);

CREATE INDEX idx_password_history_user_created_at
    ON password_history (user_id, created_at DESC);


-- ============================================================
-- UPDATED_AT FUNCTION
-- ============================================================

CREATE OR REPLACE FUNCTION set_app_user_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


-- ============================================================
-- UPDATED_AT TRIGGER
-- ============================================================

CREATE TRIGGER trg_app_user_updated_at
BEFORE UPDATE ON app_user
FOR EACH ROW
EXECUTE FUNCTION set_app_user_updated_at();
