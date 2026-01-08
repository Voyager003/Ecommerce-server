-- E-commerce Database Initialization Script
-- This script runs automatically when the MySQL container starts for the first time

-- Set character set
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Grant privileges to application user
GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce'@'%';
FLUSH PRIVILEGES;

-- Note: Schema creation is handled by Spring JPA (ddl-auto: create/validate)
-- This file is for additional initialization if needed
