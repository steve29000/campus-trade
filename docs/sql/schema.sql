-- CampusTrade AI 数据库初始化脚本（每服务独立库）
-- 适用于 MySQL 8.x。本地可对 docker 容器执行：
--   docker exec -i campus-mysql mysql -uroot -pcampus1234 < docs/sql/schema.sql
-- campus-ai 为无状态 mock，不需要数据库。

-- ============ campus-user ============
CREATE DATABASE IF NOT EXISTS campus_user_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS campus_user_db.`user` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============ campus-product ============
CREATE DATABASE IF NOT EXISTS campus_product_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS campus_product_db.product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  seller_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  description VARCHAR(1024) NOT NULL,
  category VARCHAR(64) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_seller_id (seller_id),
  KEY idx_status (status),
  KEY idx_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============ campus-order ============
CREATE DATABASE IF NOT EXISTS campus_order_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS campus_order_db.orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_title VARCHAR(128) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_buyer_id (buyer_id),
  KEY idx_seller_id (seller_id),
  KEY idx_product_id (product_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============ campus-message ============
CREATE DATABASE IF NOT EXISTS campus_message_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS campus_message_db.message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  content VARCHAR(1024) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_product_id (product_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
