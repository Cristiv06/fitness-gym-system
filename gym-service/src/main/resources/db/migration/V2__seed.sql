INSERT INTO trainer (full_name, specialization, phone, email, username) VALUES
    ('Radu Pavel', 'Strength & Conditioning', '+40 731 444 444', 'radu.pavel@example.com', 'admin'),
    ('Elena Dinu', 'Pilates', '+40 732 555 555', 'elena.dinu@example.com', NULL);

INSERT INTO room (name, max_capacity) VALUES
    ('Main Hall', 30),
    ('Studio A', 20);

INSERT INTO equipment (name) VALUES
    ('Benzi elastice'),
    ('Gantere reglabile'),
    ('Covorase yoga');

INSERT INTO room_equipment (room_id, equipment_id) VALUES
    (1, 1), (1, 2), (2, 3);

INSERT INTO gym_class (trainer_id, room_id, title, start_time, end_time, max_participants) VALUES
    (1, 1, 'HIIT', CURRENT_TIMESTAMP + INTERVAL 1 DAY, CURRENT_TIMESTAMP + INTERVAL 1 DAY + INTERVAL 1 HOUR, 25),
    (2, 2, 'Pilates Intro', CURRENT_TIMESTAMP + INTERVAL 2 DAY, CURRENT_TIMESTAMP + INTERVAL 2 DAY + INTERVAL 1 HOUR, 18);

-- member_id 1 and 2 correspond to user_service_db members
INSERT INTO class_enrollment (member_id, class_id) VALUES
    (1, 1), (2, 1), (1, 2);

INSERT INTO check_in (member_id, checkin_time) VALUES
    (1, CURRENT_TIMESTAMP - INTERVAL 2 HOUR),
    (2, CURRENT_TIMESTAMP - INTERVAL 1 HOUR);
