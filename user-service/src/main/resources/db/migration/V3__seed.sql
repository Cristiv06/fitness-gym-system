INSERT INTO membership_plan (name, duration_months, price, description) VALUES
    ('Basic', 1, 150.00, 'Acces standard sala fitness'),
    ('Standard', 3, 400.00, 'Acces sala + 2 clase/saptamana'),
    ('Premium', 12, 1400.00, 'Acces complet + clase nelimitate');

INSERT INTO member (email, username, full_name, phone, date_of_birth) VALUES
    ('ana.popescu@example.com', 'user', 'Ana Popescu', '+40 721 111 111', '1997-04-16'),
    ('mihai.ionescu@example.com', NULL, 'Mihai Ionescu', '+40 722 222 222', '1993-11-08'),
    ('ioana.marin@example.com', NULL, 'Ioana Marin', '+40 723 333 333', '2000-02-03');

INSERT INTO member_profile (member_id, emergency_contact, notes) VALUES
    (1, 'Ion Popescu +40 721 000 000', 'Preferinta: dimineata'),
    (2, 'Maria Ionescu +40 722 000 000', NULL);

INSERT INTO subscription (member_id, plan_id, start_date, end_date, status) VALUES
    (1, 3, CURRENT_DATE - INTERVAL 2 MONTH, CURRENT_DATE + INTERVAL 10 MONTH, 'ACTIVE'),
    (2, 2, CURRENT_DATE - INTERVAL 1 MONTH, CURRENT_DATE + INTERVAL 2 MONTH, 'ACTIVE'),
    (3, 1, CURRENT_DATE - INTERVAL 2 MONTH, CURRENT_DATE - INTERVAL 1 MONTH, 'EXPIRED');
