CREATE TABLE membership_plan (
    plan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    duration_months INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_plan_duration CHECK (duration_months > 0),
    CONSTRAINT chk_plan_price CHECK (price >= 0)
);

CREATE TABLE member (
    member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL UNIQUE,
    username VARCHAR(50) UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    date_of_birth DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE member_profile (
    member_id BIGINT NOT NULL PRIMARY KEY,
    emergency_contact VARCHAR(120),
    notes VARCHAR(500),
    CONSTRAINT fk_member_profile_member FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
);

CREATE TABLE subscription (
    subscription_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_subscription_plan FOREIGN KEY (plan_id) REFERENCES membership_plan (plan_id),
    CONSTRAINT chk_subscription_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_subscription_member_status ON subscription (member_id, status, end_date);
