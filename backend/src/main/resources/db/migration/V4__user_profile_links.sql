ALTER TABLE member
    ADD COLUMN username VARCHAR(50) NULL,
    ADD CONSTRAINT uq_member_username UNIQUE (username),
    ADD CONSTRAINT fk_member_user FOREIGN KEY (username) REFERENCES users (username) ON DELETE SET NULL;

ALTER TABLE trainer
    ADD COLUMN username VARCHAR(50) NULL,
    ADD CONSTRAINT uq_trainer_username UNIQUE (username),
    ADD CONSTRAINT fk_trainer_user FOREIGN KEY (username) REFERENCES users (username) ON DELETE SET NULL;

UPDATE member
SET username = 'user'
WHERE member_id = 1
  AND username IS NULL;
