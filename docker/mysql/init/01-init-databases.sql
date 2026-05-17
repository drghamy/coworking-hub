CREATE DATABASE IF NOT EXISTS coworking_db;
CREATE DATABASE IF NOT EXISTS coworking_main_db;

CREATE USER IF NOT EXISTS 'springstudent'@'%' IDENTIFIED BY 'springstudent';

GRANT ALL PRIVILEGES ON coworking_db.* TO 'springstudent'@'%';
GRANT ALL PRIVILEGES ON coworking_main_db.* TO 'springstudent'@'%';

FLUSH PRIVILEGES;
