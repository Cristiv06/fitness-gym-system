CREATE TABLE users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_authorities ON authorities (username, authority);

CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) NOT NULL PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

ALTER TABLE member
    ADD CONSTRAINT fk_member_user FOREIGN KEY (username) REFERENCES users (username) ON DELETE SET NULL;

INSERT INTO users (username, password, enabled) VALUES
    ('admin', '$2b$10$M7X9k2F9tVWVBODyM7JuI.VpMRvmoenkXkqBKkvxbhXwMZb7d.91S', TRUE),
    ('user', '$2b$10$W6D4V81VWXxeZjCKFXZGu.ZBbT9jnXYviYJeOWdJ0yFsHiq4zV3hG', TRUE);

INSERT INTO authorities (username, authority) VALUES
    ('admin', 'ROLE_ADMIN'),
    ('admin', 'ROLE_USER'),
    ('user', 'ROLE_USER');
