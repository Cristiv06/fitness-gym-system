CREATE TABLE membership_plan (
    plan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    duration_months INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_membership_plan_duration CHECK (duration_months > 0),
    CONSTRAINT chk_membership_plan_price CHECK (price >= 0)
);

CREATE TABLE member (
    member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    date_of_birth DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trainer (
    trainer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    specialization VARCHAR(120),
    phone VARCHAR(30),
    email VARCHAR(150)
);

CREATE TABLE room (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE,
    max_capacity INT NOT NULL,
    CONSTRAINT chk_room_capacity CHECK (max_capacity > 0)
);

CREATE TABLE subscription (
    subscription_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_member
        FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_subscription_plan
        FOREIGN KEY (plan_id) REFERENCES membership_plan (plan_id),
    CONSTRAINT chk_subscription_dates CHECK (end_date >= start_date)
);

CREATE TABLE gym_class (
    class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trainer_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_participants INT NOT NULL,
    CONSTRAINT fk_gym_class_trainer
        FOREIGN KEY (trainer_id) REFERENCES trainer (trainer_id),
    CONSTRAINT fk_gym_class_room
        FOREIGN KEY (room_id) REFERENCES room (room_id),
    CONSTRAINT chk_gym_class_time CHECK (end_time > start_time),
    CONSTRAINT chk_gym_class_capacity CHECK (max_participants > 0)
);

CREATE TABLE class_enrollment (
    enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_enrollment_member
        FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_class_enrollment_class
        FOREIGN KEY (class_id) REFERENCES gym_class (class_id),
    CONSTRAINT uq_class_enrollment_member_class UNIQUE (member_id, class_id)
);

CREATE TABLE check_in (
    checkin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    checkin_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_checkin_member
        FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE INDEX idx_subscription_member_status_end_date
    ON subscription (member_id, status, end_date);
CREATE INDEX idx_gym_class_start_time
    ON gym_class (start_time);
CREATE INDEX idx_class_enrollment_class
    ON class_enrollment (class_id);
CREATE INDEX idx_checkin_member_time
    ON check_in (member_id, checkin_time);
