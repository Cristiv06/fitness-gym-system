CREATE TABLE trainer (
    trainer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    specialization VARCHAR(120),
    phone VARCHAR(30),
    email VARCHAR(150),
    username VARCHAR(50) UNIQUE
);

CREATE TABLE room (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE,
    max_capacity INT NOT NULL,
    CONSTRAINT chk_room_capacity CHECK (max_capacity > 0)
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

CREATE TABLE gym_class (
    class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trainer_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_participants INT NOT NULL,
    CONSTRAINT fk_gym_class_trainer FOREIGN KEY (trainer_id) REFERENCES trainer (trainer_id),
    CONSTRAINT fk_gym_class_room FOREIGN KEY (room_id) REFERENCES room (room_id),
    CONSTRAINT chk_gym_class_time CHECK (end_time > start_time),
    CONSTRAINT chk_gym_class_capacity CHECK (max_participants > 0)
);

-- member_id references user_service_db.member - no FK constraint (cross-service)
CREATE TABLE class_enrollment (
    enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_enrollment_class FOREIGN KEY (class_id) REFERENCES gym_class (class_id),
    CONSTRAINT uq_class_enrollment_member_class UNIQUE (member_id, class_id)
);

-- member_id references user_service_db.member - no FK constraint (cross-service)
CREATE TABLE check_in (
    checkin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    checkin_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_gym_class_start_time ON gym_class (start_time);
CREATE INDEX idx_class_enrollment_class ON class_enrollment (class_id);
CREATE INDEX idx_class_enrollment_member ON class_enrollment (member_id);
CREATE INDEX idx_checkin_member ON check_in (member_id, checkin_time);
