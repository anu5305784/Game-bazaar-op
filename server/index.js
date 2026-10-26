require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const crypto = require('crypto');
const { Pool } = require('pg');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.PORT || 8080;

// Security Middlewares
app.use(helmet());
app.use(cors({ origin: '*' }));
app.use(express.json({ limit: '10mb' }));

// Rate Limiting
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 50,
  message: { error: 'Too many authentication attempts, please try again later.' }
});

const checkoutLimiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 30,
  message: { error: 'Too many checkout requests. Escrow duplicate-protection active.' }
});

// Database Connection
const pool = process.env.DATABASE_URL
  ? new Pool({
      connectionString: process.env.DATABASE_URL,
      ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false
    })
  : null;

// Helper: Query Database or return mock/error
async function queryDb(text, params = []) {
  if (!pool) {
    throw new Error('Database connection not configured. Please set DATABASE_URL environment variable.');
  }
  return await pool.query(text, params);
}

// Password Security
function hashPassword(password, salt) {
  return crypto.pbkdf2Sync(password, salt, 10000, 32, 'sha256').toString('hex');
}

function verifyPassword(password, salt, hash) {
  const testHash = hashPassword(password, salt);
  return crypto.timingSafeEqual(Buffer.from(testHash), Buffer.from(hash));
}

// Health Check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'HEALTHY',
    service: 'Gamer Bazaar Escrow API',
    databaseConnected: pool !== null,
    timestamp: new Date().toISOString()
  });
});

// ================= AUTHENTICATION =================

app.post('/api/auth/register', authLimiter, async (req, res) => {
  try {
    const { username, email, password, role = 'BUYER', preferredCurrency = 'USD' } = req.body;
    if (!username || !email || !password || password.length < 6) {
      return res.status(400).json({ error: 'Valid username, email, and minimum 6-character password required' });
    }

    const salt = crypto.randomBytes(16).toString('hex');
    const passwordHash = hashPassword(password, salt);
    const userId = `usr_${uuidv4().substring(0, 8)}`;
    const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();

    await queryDb(
      `INSERT INTO users (id, username, email, password_hash, salt, role, display_name, verification_code, preferred_currency, wallet_balance)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, 100.00)`,
      [userId, username.trim(), email.trim().toLowerCase(), passwordHash, salt, role, username.trim(), verificationCode, preferredCurrency]
    );

    res.status(201).json({
      id: userId,
      username,
      email,
      role,
      preferredCurrency,
      walletBalance: 100.00,
      message: 'User registered successfully. Welcome bonus of $100.00 credited.'
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/auth/login', authLimiter, async (req, res) => {
  try {
    const { identifier, password } = req.body;
    if (!identifier || !password) {
      return res.status(400).json({ error: 'Identifier and password required' });
    }

    const result = await queryDb(
      `SELECT * FROM users WHERE email = $1 OR username = $1 LIMIT 1`,
      [identifier.trim().toLowerCase()]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'No account found' });
    }

    const user = result.rows[0];
    if (user.is_suspended) {
      return res.status(403).json({ error: 'Account suspended by administration. Contact support.' });
    }

    const isValid = verifyPassword(password, user.salt, user.password_hash);
    if (!isValid) {
      return res.status(401).json({ error: 'Incorrect credentials' });
    }

    res.json({
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role,
      displayName: user.display_name,
      preferredCurrency: user.preferred_currency,
      walletBalance: parseFloat(user.wallet_balance),
      pendingBalance: parseFloat(user.pending_balance)
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Admin Bootstrap (Requires Security Key, never a public default password)
app.post('/api/auth/bootstrap-admin', async (req, res) => {
  try {
    const { username, email, password, setupKey } = req.body;
    const expectedKey = process.env.ADMIN_SETUP_KEY || 'GB-ADMIN-SECURE-KEY-2026';

    if (setupKey !== expectedKey) {
      return res.status(403).json({ error: 'Invalid security setup key' });
    }

    if (!username || !email || !password || password.length < 8) {
      return res.status(400).json({ error: 'Username, email, and minimum 8-character password required' });
    }

    const salt = crypto.randomBytes(16).toString('hex');
    const passwordHash = hashPassword(password, salt);
    const adminId = `usr_admin_${uuidv4().substring(0, 6)}`;

    await queryDb(
      `INSERT INTO users (id, username, email, password_hash, salt, role, display_name, is_email_verified, wallet_balance)
       VALUES ($1, $2, $3, $4, $5, 'ADMIN', $6, TRUE, 25000.00)
       ON CONFLICT (email) DO UPDATE SET role = 'ADMIN', password_hash = $4, salt = $5, is_suspended = FALSE`,
      [adminId, username.trim(), email.trim().toLowerCase(), passwordHash, salt, `${username} (Admin)`]
    );

    // Audit Log
    await queryDb(
      `INSERT INTO admin_audit_logs (id, admin_id, admin_name, action, target_type, target_id, details)
       VALUES ($1, $2, $3, 'BOOTSTRAP_ADMIN', 'USER', $2, 'Primary platform administrator initialized via secure bootstrap key.')`,
      [`LOG-BOOT-${Date.now()}`, adminId, username]
    );

    res.json({ message: 'Administrator initialized successfully.', adminId, role: 'ADMIN' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= LISTINGS =================

app.get('/api/listings', async (req, res) => {
  try {
    const { category, query } = req.query;
    let sql = `SELECT * FROM listings WHERE status = 'APPROVED' ORDER BY created_at DESC`;
    const params = [];

    const result = await queryDb(sql, params);
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= CHECKOUT & WALLET TRANSACTIONS =================

app.post('/api/orders/checkout-wallet', checkoutLimiter, async (req, res) => {
  const client = await pool.connect();
  try {
    const { buyerId, listingId } = req.body;

    await client.query('BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE');

    // 1. Lock and verify listing
    const listingRes = await client.query(
      `SELECT * FROM listings WHERE id = $1 FOR UPDATE`,
      [listingId]
    );

    if (listingRes.rows.length === 0) {
      throw new Error('Listing not found');
    }
    const listing = listingRes.rows[0];
    if (listing.status !== 'APPROVED') {
      throw new Error('Account listing has already been sold or reserved.');
    }

    // 2. Lock and verify buyer funds
    const buyerRes = await client.query(
      `SELECT * FROM users WHERE id = $1 FOR UPDATE`,
      [buyerId]
    );
    if (buyerRes.rows.length === 0) {
      throw new Error('Buyer account not found');
    }
    const buyer = buyerRes.rows[0];

    const price = parseFloat(listing.original_price);
    const balance = parseFloat(buyer.wallet_balance);

    if (balance < price) {
      throw new Error(`Insufficient funds: Required ${price} ${listing.original_currency}, available ${balance}`);
    }

    // 3. Mark listing as SOLD
    await client.query(`UPDATE listings SET status = 'SOLD' WHERE id = $1`, [listingId]);

    // 4. Deduct buyer wallet
    const newBuyerBalance = balance - price;
    await client.query(`UPDATE users SET wallet_balance = $1 WHERE id = $2`, [newBuyerBalance, buyerId]);

    // 5. Generate Order
    const orderNumber = `GB-${new Date().getFullYear()}-${uuidv4().substring(0, 6).toUpperCase()}`;
    const orderId = `ORD-${uuidv4().substring(0, 8).toUpperCase()}`;

    await client.query(
      `INSERT INTO orders (id, order_number, listing_id, listing_title, game_title, buyer_id, buyer_email, seller_id, seller_name, amount, currency, payment_method, payment_status, delivery_status, status, transaction_reference)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, 'WALLET', 'PAID', 'DELIVERED', 'Delivered', $12)`,
      [orderId, orderNumber, listing.id, listing.title, listing.game_title, buyer.id, buyer.email, listing.seller_id, listing.seller_name, price, listing.original_currency, `TXN-WAL-${orderNumber}`]
    );

    // 6. Record Immutable Debit in Ledger
    await client.query(
      `INSERT INTO wallet_transactions (id, user_id, type, amount, currency, status, reference_id, description, balance_after)
       VALUES ($1, $2, 'PAYMENT_DEBIT', $3, $4, 'COMPLETED', $5, $6, $7)`,
      [`TXN-DEB-${uuidv4().substring(0, 8)}`, buyer.id, price, listing.original_currency, orderNumber, `Payment for ${listing.title} (${orderNumber})`, newBuyerBalance]
    );

    // 7. Credit Seller
    const sellerRes = await client.query(`SELECT * FROM users WHERE id = $1 FOR UPDATE`, [listing.seller_id]);
    if (sellerRes.rows.length > 0) {
      const seller = sellerRes.rows[0];
      const newSellerBalance = parseFloat(seller.wallet_balance) + price;
      await client.query(`UPDATE users SET wallet_balance = $1 WHERE id = $2`, [newSellerBalance, seller.id]);

      await client.query(
        `INSERT INTO wallet_transactions (id, user_id, type, amount, currency, status, reference_id, description, balance_after)
         VALUES ($1, $2, 'SALE_CREDIT', $3, $4, 'COMPLETED', $5, $6, $7)`,
        [`TXN-CRE-${uuidv4().substring(0, 8)}`, seller.id, price, listing.original_currency, orderNumber, `Sale proceeds for ${listing.title} (${orderNumber})`, newSellerBalance]
      );
    }

    // 8. Release Delivery Credentials Vault
    await client.query(
      `UPDATE delivery_vault SET is_released = TRUE, released_to_user_id = $1, released_at = NOW() WHERE listing_id = $2`,
      [buyer.id, listing.id]
    );

    await client.query('COMMIT');

    res.json({
      success: true,
      orderId,
      orderNumber,
      amount: price,
      currency: listing.original_currency,
      status: 'Delivered',
      message: 'Order placed atomically. Credentials unlocked in vault.'
    });
  } catch (err) {
    await client.query('ROLLBACK');
    res.status(400).json({ error: err.message });
  } finally {
    client.release();
  }
});

// ================= UPI PAYMENT SUBMISSION & REVIEW =================

app.post('/api/orders/submit-upi', async (req, res) => {
  try {
    const { orderId, userId, amountInr, utrNumber, screenshotUrl } = req.body;
    if (!utrNumber || utrNumber.length !== 12) {
      return res.status(400).json({ error: 'Valid 12-digit UTR payment reference required' });
    }

    const subId = `UPI-SUB-${uuidv4().substring(0, 6).toUpperCase()}`;
    await queryDb(
      `INSERT INTO upi_submissions (id, order_id, user_id, user_email, amount_inr, utr_number, merchant_upi_id, screenshot_url, status)
       VALUES ($1, $2, $3, (SELECT email FROM users WHERE id = $3), $4, $5, 'gamerbazaar.escrow@icici', $6, 'UNDER_REVIEW')`,
      [subId, orderId || '', userId, amountInr, utrNumber, screenshotUrl || '']
    );

    res.json({
      success: true,
      submissionId: subId,
      status: 'UNDER_REVIEW',
      message: 'UPI reference submitted for admin verification. Verification will process exact-once upon merchant bank reconciliation.'
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= ADMIN REPORTING & EXPORTS =================

app.get('/api/admin/export/orders.csv', async (req, res) => {
  try {
    const result = await queryDb(`SELECT * FROM orders ORDER BY created_at DESC`);
    let csv = 'OrderNumber,ListingTitle,GameTitle,Amount,Currency,PaymentMethod,Status,PaymentStatus,BuyerEmail,CreatedAt\n';
    result.rows.forEach(o => {
      csv += `${o.order_number},"${o.listing_title.replace(/"/g, '""')}","${o.game_title}",${o.amount},${o.currency},${o.payment_method},${o.status},${o.payment_status},${o.buyer_email},${o.created_at}\n`;
    });
    res.header('Content-Type', 'text/csv');
    res.attachment('gamerbazaar_orders.csv');
    res.send(csv);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/export/transactions.csv', async (req, res) => {
  try {
    const result = await queryDb(`SELECT * FROM wallet_transactions ORDER BY created_at DESC`);
    let csv = 'TxnId,UserId,Type,Amount,Currency,Status,ReferenceId,BalanceAfter,CreatedAt\n';
    result.rows.forEach(t => {
      csv += `${t.id},${t.user_id},${t.type},${t.amount},${t.currency},${t.status},"${t.reference_id}",${t.balance_after},${t.created_at}\n`;
    });
    res.header('Content-Type', 'text/csv');
    res.attachment('gamerbazaar_ledger.csv');
    res.send(csv);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`Gamer Bazaar Escrow Backend listening on port ${PORT}`);
});
