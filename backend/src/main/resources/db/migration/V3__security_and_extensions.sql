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

CREATE UNIQUE INDEX uq_authorities_username_authority ON authorities (username, authority);

CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) NOT NULL PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

CREATE TABLE member_profile (
    member_id BIGINT NOT NULL PRIMARY KEY,
    emergency_contact VARCHAR(120),
    notes VARCHAR(500),
    CONSTRAINT fk_member_profile_member FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
);

CREATE TABLE equipment (
    equipment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE room_equipment (
    room_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    PRIMARY KEY (room_id, equipment_id),
    CONSTRAINT fk_room_equipment_room FOREIGN KEY (room_id) REFERENCES room (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_room_equipment_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (equipment_id) ON DELETE CASCADE
);

INSERT INTO users (username, password, enabled) VALUES
    ('admin', '$2b$10$M7X9k2F9tVWVBODyM7JuI.VpMRvmoenkXkqBKkvxbhXwMZb7d.91S', TRUE),
    ('user', '$2b$10$W6D4V81VWXxeZjCKFXZGu.ZBbT9jnXYviYJeOWdJ0yFsHiq4zV3hG', TRUE);

INSERT INTO authorities (username, authority) VALUES
    ('admin', 'ROLE_ADMIN'),
    ('admin', 'ROLE_USER'),
    ('user', 'ROLE_USER');

INSERT INTO member_profile (member_id, emergency_contact, notes) VALUES
    (1, 'Ion Popescu +40 721 000 000', 'Preferinta: dimineata'),
    (2, 'Maria Ionescu +40 722 000 000', NULL);

INSERT INTO equipment (name) VALUES
    ('Benzi elastice'),
    ('Gantere reglabile'),
    ('Covorase yoga');

INSERT INTO room_equipment (room_id, equipment_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3);
