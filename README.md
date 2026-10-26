# ☕ KOFEIA Coffee Shop - Mobile POS & Inventory System

A fast, lightweight, and reliable Android Point of Sale (POS) and Inventory Management System designed specifically for **Kofeia Coffee Shop**. Built natively with Java and local SQLite storage for seamless, 100% offline-ready operations.

---

## 📌 Table of Contents
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Database Architecture](#-database-architecture)
- [Default Admin Credentials](#-default-admin-credentials)
- [Getting Started & Installation](#-getting-started--installation)

---

## ✨ Features

### 1. Menu & Category Browsing
* **Diverse Beverage Categories:**
  * **Espresso:** Americano, Spanish Latte, Caramel Macchiato, Pistachio Espresso, etc.
  * **Non-Coffee:** Oreo Choco Cloud, Ube Tres Leches, Strawberry Chocolate, etc.
  * **Matcha:** Matcha Latte, Strawberry Matcha, Matcha Biscoff, Sea Salt Matcha, etc.
  * **Refreshment:** Fruit Teas, Lemonades/Ades, Sparkleberry
  * **Signature:** Kofeia Hot Choco
  * **Add Ons:** Extra Espresso Shot, Oat Milk substitution, Flavored Syrups, Cream
* **Multi-Size Selection:** Easy toggle between **Medio** and **Grande** cup sizes with dynamic pricing.

### 2. Smart Inventory & Packaging Deduction
* **Real-Time Stock Checks:** Prevents ordering sold-out products.
* **Automatic Supply Deductions Per Order:**
  * Medio & Grande Paper Cups
  * Cup Lids & Coffee Straws
  * Takeout Paper Bags
* **Strict Validation:** Automatically stops checkout if required packaging supplies (such as Takeout Paper Bags for takeout orders) are out of stock.

### 3. Flexible Checkout & Discounts
* **Senior Citizen / PWD Discount:** Built-in 20% discount calculation applied across order totals.
* **Payment Methods:**
  * **Cash:** Real-time change calculation with an "Exact Amount" shortcut.
  * **E-Wallet Demo (GCash / Maya):** Interactive QR code display with reference number logging.
* **Order Types:** Dine-In or Takeout options.

### 4. Digital Invoicing & PDF Printing
* **WebView Receipt Rendering:** Generates clean, professional receipts complete with invoice numbers (e.g., `INV-000001`), timestamp, item breakdown, applied discounts, and payment summary.
* **Print / Export to PDF:** Integrated printing capability to physical printers or export directly as a PDF file.

### 5. Admin Dashboard & Sales Analytics
* **Secure Admin Access:** Protected by administrative credentials.
* **Sales Reports:** Filter transactions by custom date ranges or view grouped monthly summaries.
* **Best Seller Identification:** Analyzes transaction records to highlight top-selling items.
* **Low Stock Alerts:** Highlights items and supplies with 10 units or fewer remaining.
* **Stock Management:** Allows manual updates to supply quantities directly inside the app.

---

## 🛠 Tech Stack

| Component | Technology |
| :--- | :--- |
| **Operating System** | Android |
| **Language** | Java |
| **Database** | SQLite (`kofeia.db` via `SQLiteOpenHelper`) |
| **IDE** | Android Studio |
| **Minimum SDK** | API Level 24 (Android 7.0 Nougat) |
| **Target SDK** | Modern Android Versions (Android 14/15 compatible) |

---

## 🗄 Database Architecture

The application uses an embedded SQLite database containing the following tables:
* `products` — Stores item name, category, hot/iced prices, and product stock.
* `supplies` — Tracks inventory for packaging materials (Medio Cups, Grande Cups, Lids, Straws, Takeout Bags).
* `orders` — Records transaction headers (date, total, cash received, change, order type, payment method, reference number).
* `order_items` — Stores line items tied to each order ID.
* `admin` — Holds administrator authentication credentials.

---

## 🔐 Default Admin Credentials

Use these credentials to access the Admin Dashboard and Sales Reports:
* **Username:** `admin`
* **Password:** `admin123`

---

## 🚀 Getting Started & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/USERNAME/REPO_NAME.git
