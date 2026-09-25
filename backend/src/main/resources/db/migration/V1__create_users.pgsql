CREATE TABLE users (
    id              uuid         PRIMARY KEY,
    username        varchar(50)  NOT NULL,
    encrypted_email varchar(100) NOT NULL,
    password_hash   varchar(255) NOT NULL,
    created_at      timestamptz  NOT NULL,
    updated_at      timestamptz  NOT NULL,

    CONSTRAINT uk_users_username        UNIQUE (username),
    CONSTRAINT uk_users_encrypted_email UNIQUE (encrypted_email)
);
