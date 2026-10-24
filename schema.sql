-- ====================================================================
-- EXPENZO: EMPLOYEE ADVANCE & EXPENSE SETTLEMENT MANAGEMENT SYSTEM
-- Database Schema for MySQL 8.x
-- ====================================================================

CREATE DATABASE IF NOT EXISTS expenzo_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE expenzo_db;

-- 1. Users Table (Employees, Managers, Administrators)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_EMPLOYEE',
    department VARCHAR(100) DEFAULT 'General',
    phone VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Cash Advance Requests Table
CREATE TABLE IF NOT EXISTS advance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    destination VARCHAR(150),
    start_date DATE,
    end_date DATE,
    requested_amount DECIMAL(15, 2) NOT NULL,
    disbursed_amount DECIMAL(15, 2) DEFAULT NULL,
    disbursed_date DATE DEFAULT NULL,
    notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    manager_comment TEXT,
    reviewed_by_id BIGINT DEFAULT NULL,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_adv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_adv_reviewer FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_adv_user (user_id),
    INDEX idx_adv_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Expenses Incurred against Cash Advances
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    advance_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    merchant VARCHAR(150),
    description TEXT,
    receipt_filename VARCHAR(255),
    receipt_content_type VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    manager_comment TEXT,
    reviewed_by_id BIGINT DEFAULT NULL,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_exp_advance FOREIGN KEY (advance_id) REFERENCES advance_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_exp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exp_reviewer FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_exp_advance (advance_id),
    INDEX idx_exp_user (user_id),
    INDEX idx_exp_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Direct Reimbursement Claims (Personal Out-of-Pocket Expenses)
CREATE TABLE IF NOT EXISTS reimbursement_claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    merchant VARCHAR(150),
    description TEXT,
    receipt_filename VARCHAR(255),
    receipt_content_type VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    manager_comment TEXT,
    payment_reference VARCHAR(100),
    paid_date DATE DEFAULT NULL,
    reviewed_by_id BIGINT DEFAULT NULL,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_claim_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_reviewer FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_claim_user (user_id),
    INDEX idx_claim_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Advance Reconciliation and Settlements
CREATE TABLE IF NOT EXISTS settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    advance_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_disbursed_advance DECIMAL(15, 2) NOT NULL,
    total_eligible_expenses DECIMAL(15, 2) NOT NULL,
    remaining_balance DECIMAL(15, 2) NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
    submission_notes TEXT,
    manager_comment TEXT,
    payment_or_refund_reference VARCHAR(100),
    completed_date DATE DEFAULT NULL,
    reviewed_by_id BIGINT DEFAULT NULL,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_set_advance FOREIGN KEY (advance_id) REFERENCES advance_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_set_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_set_reviewer FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_set_advance (advance_id),
    INDEX idx_set_user (user_id),
    INDEX idx_set_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
