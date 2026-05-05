INSERT INTO membership_plan (name, duration_months, price, description)
VALUES
    ('Basic', 1, 150.00, 'Acces standard sala fitness'),
    ('Standard', 3, 400.00, 'Acces sala + 2 clase/saptamana'),
    ('Premium', 12, 1400.00, 'Acces complet + clase nelimitate');

INSERT INTO member (email, full_name, phone, date_of_birth)
VALUES
    ('ana.popescu@example.com', 'Ana Popescu', '+40 721 111 111', '1997-04-16'),
    ('mihai.ionescu@example.com', 'Mihai Ionescu', '+40 722 222 222', '1993-11-08'),
    ('ioana.marin@example.com', 'Ioana Marin', '+40 723 333 333', '2000-02-03');

INSERT INTO trainer (full_name, specialization, phone, email)
VALUES
    ('Radu Pavel', 'Strength & Conditioning', '+40 731 444 444', 'radu.pavel@example.com'),
    ('Elena Dinu', 'Pilates', '+40 732 555 555', 'elena.dinu@example.com');

INSERT INTO room (name, max_capacity)
VALUES
    ('Main Hall', 30),
    ('Studio A', 20);

INSERT INTO subscription (member_id, plan_id, start_date, end_date, status)
VALUES
    (1, 3, CURRENT_DATE - INTERVAL 2 MONTH, CURRENT_DATE + INTERVAL 10 MONTH, 'ACTIVE'),
    (2, 2, CURRENT_DATE - INTERVAL 1 MONTH, CURRENT_DATE + INTERVAL 2 MONTH, 'ACTIVE'),
    (3, 1, CURRENT_DATE - INTERVAL 2 MONTH, CURRENT_DATE - INTERVAL 1 MONTH, 'EXPIRED');

INSERT INTO gym_class (trainer_id, room_id, title, start_time, end_time, max_participants)
VALUES
    (1, 1, 'HIIT', CURRENT_TIMESTAMP + INTERVAL 1 DAY, CURRENT_TIMESTAMP + INTERVAL 1 DAY + INTERVAL 1 HOUR, 25),
    (2, 2, 'Pilates Intro', CURRENT_TIMESTAMP + INTERVAL 2 DAY, CURRENT_TIMESTAMP + INTERVAL 2 DAY + INTERVAL 1 HOUR, 18);

INSERT INTO class_enrollment (member_id, class_id)
VALUES
    (1, 1),
    (2, 1),
    (1, 2);

INSERT INTO check_in (member_id, checkin_time)
VALUES
    (1, CURRENT_TIMESTAMP - INTERVAL 2 HOUR),
    (2, CURRENT_TIMESTAMP - INTERVAL 1 HOUR);
