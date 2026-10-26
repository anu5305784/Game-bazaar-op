# GAMER BAZAAR — Global Gaming Account Marketplace & Escrow Platform

A secure, database-backed global gaming account marketplace built with **Android Jetpack Compose**, a **PostgreSQL relational database**, and a production-grade **Node.js/Express escrow backend**.

---

## 🌟 Key Features & Capabilities

### 1. Buyer Dashboard & 7 Order Statuses
- **Orders View**: Complete tracking across all 7 mandated statuses:
  1. `Pending Payment`
  2. `Payment Under Review`
  3. `Paid`
  4. `Processing`
  5. `Delivered`
  6. `Refunded`
  7. `Disputed`
- **Decrypted Vault Reveal**: AES-256-GCM encrypted account login credentials and email access tokens are revealed only upon verified payment confirmation.
- **Support & Disputes**: In-app dispute filing with escrow arbitration.
- **Favourites & Notifications**: Real-time push/in-app status alerts.

### 2. Buying System & Double-Spend Protection
- **Multi-Currency**: Live conversions across 12 world currencies (USD, INR, EUR, GBP, AED, JPY, CAD, AUD, etc.) displaying converted and original prices.
- **Race Condition Prevention**: Database row locking (`FOR UPDATE` in PostgreSQL, `withTransaction` in Room) prevents two buyers from purchasing the same unique game account.
- **Unique References**: Generates cryptographically unique order IDs (e.g. `GB-2026-X8921`) and transaction hashes.

### 3. Immutable Wallet & Ledger
- **Atomic Balance Control**: Funds debited/credited strictly via database transactions. Negative balances and duplicate deductions are strictly prohibited.
- **Ledger Invariant**: Immutable transaction history (`PAYMENT_DEBIT`, `SALE_CREDIT`, `REFUND_CREDIT`, `DEPOSIT_UPI`).

### 4. Verified UPI & Crypto Payments
- **UPI Indian Payments**: Configured Merchant VPA (`gamerbazaar.escrow@icici`), 12-digit UTR input, and screenshot reference.
- **Zero-Trust Verification**: UTR and screenshots are sent to the administrative review queue. Wallet balances and order delivery are never credited on client assertion alone.
- **Crypto Payments**: Supported coins (USDT, BTC, ETH, SOL) with testnet sandbox toggling.

### 5. Comprehensive Admin Panel
- **Overview Statistics**: Platform GMV, active listings, escrow volume, open disputes, and pending reviews.
- **Listings Review**: One-click approval or rejection with mandatory rejection reasons.
- **UPI Payment Verification**: Review UTR, amount, and screenshots with exact-once approval/rejection.
- **Disputes & Refunds**: Arbitrate disputes with atomic refunds credited directly back to the buyer's wallet.
- **User Management**: View users, suspend/reactivate accounts, and elevate roles.
- **Game Catalog**: Manage 350+ game titles and categories.
- **Showcase Management**: Feature and showcase listings.
- **Reports Queue**: Review buyer-reported suspicious listings with instant suspension capability.
- **Financial Ledger**: Full audit trail of platform debits and credits.
- **CSV Reports**: Export standardized CSVs for Orders, Ledger, and Audit Log.
- **Compliance Audit Log**: Chronological administrative action history.
- **Secure First-Admin Bootstrap**: Zero public default passwords; uses high-entropy salted hashes and a secure setup key (`GB-ADMIN-SECURE-KEY-2026`).

---

## 🏗️ Architecture Overview

```
                      ┌───────────────────────────────────────┐
                      │    Gamer Bazaar Android Client        │
                      │  (Kotlin, Jetpack Compose, Material 3)│
                      └──────────────────┬────────────────────┘
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 │                                               │
      (Local Offline Cache)                             (Production API)
                 ▼                                               ▼
┌─────────────────────────────────┐             ┌────────────────────────────────┐
│   Android Local Database (Room) │             │    Gamer Bazaar Node.js API    │
│  - Users, Orders, Listings      │             │  - Express, Helmet, Rate Limit │
│  - Wallet Ledger, Audit Logs    │             │  - Atomic Escrow Transactions  │
└─────────────────────────────────┘             └────────────────┬───────────────┘
                                                                 │
                                                                 ▼
                                                ┌────────────────────────────────┐
                                                │   PostgreSQL 14+ Database      │
                                                │  - 12 Relational Tables        │
                                                │  - Foreign Keys & Constraints  │
                                                └────────────────────────────────┘
```

---

## 🚀 Local Development Setup

### 1. Android Application
1. Open the project root in **Android Studio** (or view directly via AI Studio Streaming Emulator).
2. Sync Gradle files (`build.gradle.kts` and `settings.gradle.kts`).
3. Build the debug APK:
   ```bash
   gradle assembleDebug
   ```
4. Run unit tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```

### 2. Local Backend Server
1. Navigate to the `server` directory:
   ```bash
   cd server
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Copy environment configuration:
   ```bash
   cp ../.env.example .env
   ```
4. Start the server:
   ```bash
   npm start
   ```
   The server will start on `http://localhost:8080`.

---

## 🗄️ Database Setup & Migrations (PostgreSQL)

1. Create a PostgreSQL database:
   ```bash
   createdb gamerbazaar
   ```
2. Apply the relational schema and seed configurations:
   ```bash
   psql -d gamerbazaar -f schema.sql
   ```
3. Verify tables:
   ```sql
   \dt
   -- Confirms: users, games, listings, delivery_vault, orders,
   -- wallet_transactions, upi_submissions, support_tickets,
   -- listing_reports, notifications, system_configs, admin_audit_logs
   ```

---

## 📦 How to Upload to GitHub

1. Initialize git in the project root:
   ```bash
   git init
   git add .
   git commit -m "feat: complete Gamer Bazaar escrow platform with admin panel and PostgreSQL deployment"
   ```
2. Create a new repository on [GitHub](https://github.com/new) named `gamer-bazaar`.
3. Link your remote repository and push:
   ```bash
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/gamer-bazaar.git
   git push -u origin main
   ```

---

## ☁️ Deploying on Render (Step-by-Step)

### Method A: Automated Deployment with Render Blueprint (Recommended)
1. Sign in to your [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** and select **Blueprint**.
3. Connect your GitHub repository (`gamer-bazaar`).
4. Render will automatically detect `render.yaml` and provision:
   - **`gamer-bazaar-db`**: Managed PostgreSQL Database.
   - **`gamer-bazaar-api`**: Web Service running the Node.js API with all environment variables wired automatically.
5. Click **Apply**.
6. Once provisioned, initialize the PostgreSQL schema by connecting to the Render DB via External Connection string or Render Shell:
   ```bash
   psql "<RENDER_EXTERNAL_DATABASE_URL>" -f schema.sql
   ```

### Method B: Manual Service Creation
1. **Create PostgreSQL Database**:
   - Go to **New +** > **PostgreSQL**.
   - Name: `gamer-bazaar-db`.
   - Database: `gamerbazaar`.
   - User: `gamerbazaar_user`.
   - Copy the **Internal Database URL**.
2. **Create Web Service**:
   - Go to **New +** > **Web Service**.
   - Root Directory: `server`.
   - Build Command: `npm install`.
   - Start Command: `npm start`.
   - Environment Variables:
     - `DATABASE_URL`: Paste the Internal Database URL from Step 1.
     - `ADMIN_SETUP_KEY`: Set your secret bootstrap key (e.g. `GB-ADMIN-SECURE-KEY-2026`).
     - `UPI_MERCHANT_VPA`: `gamerbazaar.escrow@icici`.
     - `NODE_ENV`: `production`.

---

## 🔒 Custom Domain & HTTPS Setup

1. In the Render Dashboard, open your `gamer-bazaar-api` Web Service.
2. Navigate to **Settings** > **Custom Domains**.
3. Add your custom domain (e.g., `api.gamerbazaar.com`).
4. In your DNS provider (e.g., Cloudflare, Namecheap, GoDaddy):
   - Add a `CNAME` record:
     - Name: `api`
     - Value: `gamer-bazaar-api.onrender.com`
5. Render automatically issues and renews free **Let's Encrypt TLS/SSL certificates** within minutes.

---

## 🛡️ Security Features & Operational Checklist

| Feature | Operational Status | Notes |
| :--- | :--- | :--- |
| **Password Hashing** | Active | PBKDF2 with SHA-256 and unique 16-byte random salts. |
| **Credential Vault** | Active | AES-256-GCM encrypted transfer records. |
| **Atomic Wallet Deductions** | Active | Enforces non-negative balance checks inside DB transactions. |
| **UPI Verification Queue** | Active | Admin exact-once verification with mandatory rejection reasons. |
| **Dispute Arbitration** | Active | Admin arbitration with full atomic refunds to buyer wallets. |
| **Audit Log Trail** | Active | Every administrative action logged chronologically with admin ID. |
| **CSV Exports** | Active | Orders, Transactions, and Audit Logs formatted for reporting. |
| **Live UPI Bank Webhook** | Config Required | Requires merchant agreement with bank (e.g., ICICI/Razorpay) to receive automated push callbacks. |
| **Production Crypto Provider** | Config Required | Requires merchant API key from NOWPayments or Coinbase Commerce (set in `.env`). |
