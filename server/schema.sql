-- ====================================================================
-- GAMER BAZAAR — GLOBAL GAMING ACCOUNT MARKETPLACE
-- PRODUCTION RELATIONAL DATABASE SCHEMA (POSTGRESQL 14+)
-- ====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'BUYER' CHECK (role IN ('BUYER', 'SELLER', 'ADMIN')),
    display_name VARCHAR(100) NOT NULL,
    avatar_url TEXT DEFAULT '',
    is_email_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(10) DEFAULT '',
    preferred_currency VARCHAR(10) DEFAULT 'USD',
    wallet_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (wallet_balance >= 0.00),
    pending_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (pending_balance >= 0.00),
    is_suspended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

CREATE TABLE IF NOT EXISTS games (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    is_popular BOOLEAN DEFAULT FALSE,
    icon_url TEXT DEFAULT '',
    banner_url TEXT DEFAULT '',
    active_listings_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS listings (
    id VARCHAR(64) PRIMARY KEY,
    seller_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seller_name VARCHAR(100) NOT NULL,
    seller_rating NUMERIC(3, 2) DEFAULT 4.90,
    game_id VARCHAR(64) NOT NULL REFERENCES games(id) ON DELETE RESTRICT,
    game_title VARCHAR(150) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    original_price NUMERIC(12, 2) NOT NULL CHECK (original_price > 0),
    original_currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    account_level INT DEFAULT 1,
    rank VARCHAR(50) DEFAULT 'Unranked',
    skins_count INT DEFAULT 0,
    characters_count INT DEFAULT 0,
    items_description TEXT DEFAULT '',
    region VARCHAR(50) DEFAULT 'Global',
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SOLD')),
    is_featured BOOLEAN DEFAULT FALSE,
    is_showcase BOOLEAN DEFAULT FALSE,
    image_urls_json TEXT DEFAULT '[]',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS delivery_vault (
    listing_id VARCHAR(64) PRIMARY KEY REFERENCES listings(id) ON DELETE CASCADE,
    encrypted_credentials TEXT NOT NULL,
    transfer_method VARCHAR(100) DEFAULT 'Verified Account Credentials',
    transfer_instructions TEXT DEFAULT '',
    is_released BOOLEAN DEFAULT FALSE,
    released_to_user_id VARCHAR(64) REFERENCES users(id),
    released_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(64) PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    listing_id VARCHAR(64) NOT NULL REFERENCES listings(id),
    listing_title VARCHAR(255) NOT NULL,
    game_title VARCHAR(150) NOT NULL,
    buyer_id VARCHAR(64) NOT NULL REFERENCES users(id),
    buyer_email VARCHAR(255) NOT NULL,
    seller_id VARCHAR(64) NOT NULL REFERENCES users(id),
    seller_name VARCHAR(100) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(10) NOT NULL,
    payment_method VARCHAR(20) NOT NULL DEFAULT 'WALLET' CHECK (payment_method IN ('WALLET', 'UPI', 'CRYPTO')),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID' CHECK (payment_status IN ('PENDING', 'UNDER_REVIEW', 'PAID', 'FAILED', 'REFUNDED')),
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'DELIVERED' CHECK (delivery_status IN ('PENDING', 'PROCESSING', 'DELIVERED')),
    status VARCHAR(30) NOT NULL CHECK (status IN (
        'Pending Payment',
        'Payment Under Review',
        'Paid',
        'Processing',
        'Delivered',
        'Refunded',
        'Disputed'
    )),
    transaction_reference VARCHAR(100) DEFAULT '',
    utr_number VARCHAR(50) DEFAULT '',
    screenshot_url TEXT DEFAULT '',
    rejection_reason TEXT DEFAULT '',
    dispute_reason TEXT DEFAULT '',
    dispute_status VARCHAR(20) DEFAULT 'NONE' CHECK (dispute_status IN ('NONE', 'OPEN', 'RESOLVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL CHECK (type IN (
        'DEPOSIT_UPI',
        'DEPOSIT_CRYPTO',
        'PAYMENT_DEBIT',
        'SALE_CREDIT',
        'REFUND_CREDIT',
        'WITHDRAWAL_DEBIT',
        'DISPUTE_HOLD'
    )),
    amount NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED', 'UNDER_REVIEW', 'REJECTED', 'FAILED')),
    reference_id VARCHAR(100) NOT NULL,
    description TEXT DEFAULT '',
    balance_after NUMERIC(14, 2) NOT NULL CHECK (balance_after >= 0.00),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS upi_submissions (
    id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64),
    user_id VARCHAR(64) NOT NULL REFERENCES users(id),
    user_email VARCHAR(255) NOT NULL,
    amount_inr NUMERIC(12, 2) NOT NULL CHECK (amount_inr > 0),
    utr_number VARCHAR(30) NOT NULL,
    merchant_upi_id VARCHAR(100) NOT NULL,
    screenshot_url TEXT DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'UNDER_REVIEW' CHECK (status IN ('UNDER_REVIEW', 'APPROVED', 'REJECTED')),
    rejection_reason TEXT DEFAULT '',
    reviewed_by_admin_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS support_tickets (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id),
    user_email VARCHAR(255) NOT NULL,
    order_id VARCHAR(64) DEFAULT '',
    listing_title VARCHAR(255) DEFAULT '',
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    category VARCHAR(30) NOT NULL DEFAULT 'GENERAL' CHECK (category IN ('DISPUTE', 'ACCOUNT_DELIVERY', 'PAYMENT', 'REFUND', 'GENERAL')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_REVIEW', 'RESOLVED', 'CLOSED')),
    admin_response TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS listing_reports (
    id VARCHAR(64) PRIMARY KEY,
    listing_id VARCHAR(64) NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    listing_title VARCHAR(255) NOT NULL,
    reporter_id VARCHAR(64) NOT NULL REFERENCES users(id),
    reporter_email VARCHAR(255) NOT NULL,
    reason VARCHAR(50) NOT NULL CHECK (reason IN (
        'STOLEN_ACCOUNT',
        'MISLEADING_DESCRIPTION',
        'SUSPECTED_SCAM',
        'PROHIBITED_ITEMS',
        'OTHER'
    )),
    details TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN (
        'PENDING',
        'INVESTIGATING',
        'RESOLVED_DISMISSED',
        'RESOLVED_LISTING_REMOVED'
    )),
    admin_notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS system_configs (
    key VARCHAR(64) PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT DEFAULT '',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id VARCHAR(64) PRIMARY KEY,
    admin_id VARCHAR(64) NOT NULL,
    admin_name VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    details TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
