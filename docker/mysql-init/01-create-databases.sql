CREATE DATABASE IF NOT EXISTS user_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS gym_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notification_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON user_service_db.* TO 'gym_user'@'%';
GRANT ALL PRIVILEGES ON gym_service_db.* TO 'gym_user'@'%';
GRANT ALL PRIVILEGES ON notification_service_db.* TO 'gym_user'@'%';
FLUSH PRIVILEGES;
