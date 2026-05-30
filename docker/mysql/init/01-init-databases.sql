CREATE DATABASE IF NOT EXISTS coworking_db;
CREATE DATABASE IF NOT EXISTS coworking_booking_db;
CREATE DATABASE IF NOT EXISTS coworking_invoice_db;

CREATE USER IF NOT EXISTS 'springstudent'@'%' IDENTIFIED BY 'springstudent';

GRANT ALL PRIVILEGES ON coworking_db.* TO 'springstudent'@'%';
GRANT ALL PRIVILEGES ON coworking_booking_db.* TO 'springstudent'@'%';
GRANT ALL PRIVILEGES ON coworking_invoice_db.* TO 'springstudent'@'%';

FLUSH PRIVILEGES;
