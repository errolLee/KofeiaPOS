package com.example.kofeiapos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DEFAULT_STOCK = 50;
    public static final int LOW_STOCK = 10;

    public String lastError = "";

    public DBHelper(Context context) {
        super(context, "kofeia.db", null, 11);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, category TEXT, hot REAL, iced REAL, stock INTEGER DEFAULT 50)");

        db.execSQL("CREATE TABLE supplies (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, category TEXT, stock INTEGER)");

        db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, total REAL, cash REAL DEFAULT 0, change_amt REAL DEFAULT 0, " +
                "order_type TEXT, payment_type TEXT DEFAULT 'Cash', ref_no TEXT DEFAULT '')");

        db.execSQL("CREATE TABLE order_items (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, name TEXT, qty INTEGER, price REAL, product_id INTEGER DEFAULT 0)");

        seedMenu(db);
        seedSupplies(db);
        createAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS supplies");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS order_items");
        onCreate(db);
    }

    private void createAdmin(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS admin (id INTEGER PRIMARY KEY, " +
                "username TEXT, password TEXT)");
        db.execSQL("INSERT OR IGNORE INTO admin (id, username, password) VALUES (1, 'admin', 'admin123')");
    }

    public boolean checkAdmin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM admin WHERE username = ? AND password = ?",
                new String[]{username, password});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public boolean changePassword(String oldPass, String newPass) {
        if (!checkAdmin("admin", oldPass)) return false;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE admin SET password = ? WHERE id = 1", new Object[]{newPass});
        return true;
    }

    private void add(SQLiteDatabase db, String name, String cat, double medio, double grande) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("category", cat);
        v.put("hot", medio);
        v.put("iced", grande);
        v.put("stock", DEFAULT_STOCK);
        db.insert("products", null, v);
    }

    private void seedMenu(SQLiteDatabase db) {
        // ESPRESSO
        add(db, "Americano", "Espresso", 108, 128);
        add(db, "Cafe Latte", "Espresso", 128, 148);
        add(db, "Vanilla Latte", "Espresso", 138, 158);
        add(db, "Caramel Macchiato", "Espresso", 148, 178);
        add(db, "Sea Salt Caramel Latte", "Espresso", 148, 178);
        add(db, "Spanish Latte", "Espresso", 138, 168);
        add(db, "Mocha", "Espresso", 148, 178);
        add(db, "White Mocha", "Espresso", 148, 178);
        add(db, "Biscoff Espresso", "Espresso", 158, 188);
        add(db, "Dirty Ube Tresleches", "Espresso", 148, 178);
        add(db, "Pistachio Espresso", "Espresso", 168, 198);
        add(db, "Sea Salt Spanish Latte", "Espresso", 148, 178);
        add(db, "Cinnamon Roll Latte", "Espresso", 168, 198);
        add(db, "Butterscotch Latte", "Espresso", 158, 188);

        // NON-COFFEE
        add(db, "Oreo Choco Cloud", "Non-Coffee", 138, 168);
        add(db, "Biscoff Cloud", "Non-Coffee", 158, 188);
        add(db, "Strawberry Tresleches", "Non-Coffee", 138, 168);
        add(db, "Ube Tresleches", "Non-Coffee", 138, 168);
        add(db, "Caramel Pistachio Milk", "Non-Coffee", 168, 198);
        add(db, "Strawberry Chocolate", "Non-Coffee", 158, 188);

        // MATCHA
        add(db, "Matcha Latte", "Matcha", 138, 168);
        add(db, "Dirty Matcha", "Matcha", 148, 178);
        add(db, "Strawberry Matcha", "Matcha", 148, 178);
        add(db, "Mango Matcha", "Matcha", 148, 178);
        add(db, "Ube Matcha Latte", "Matcha", 148, 178);
        add(db, "Matcha Biscoff", "Matcha", 168, 198);
        add(db, "Sea Salt Matcha Biscoff", "Matcha", 178, 208);
        add(db, "Sea Salt Matcha Latte", "Matcha", 168, 198);
        add(db, "Matcha Pistachio", "Matcha", 178, 208);

        // REFRESHMENT
        add(db, "Honey Citron Tea", "Refreshment", 138, 168);
        add(db, "Honey Citron & Ginger Tea", "Refreshment", 138, 168);
        add(db, "Sparkleberry", "Refreshment", 138, 168);
        add(db, "Strawberry/Mango Ade", "Refreshment", 138, 168);

        // SIGNATURE
        add(db, "Kofeia Hot Choco", "Signature", 168, 198);

        // ADD ONS
        add(db, "Espresso Shot", "Add Ons", 38, 0);
        add(db, "Sub - Oat Milk", "Add Ons", 28, 0);
        add(db, "Sauce / Syrup", "Add Ons", 28, 0);
        add(db, "Cream", "Add Ons", 28, 0);
    }

    private void seedSupplies(SQLiteDatabase db) {
        db.execSQL("INSERT INTO supplies (name, category, stock) VALUES ('Medio Paper Cups', 'Packaging', 200)");
        db.execSQL("INSERT INTO supplies (name, category, stock) VALUES ('Grande Paper Cups', 'Packaging', 200)");
        db.execSQL("INSERT INTO supplies (name, category, stock) VALUES ('Cup Lids / Takip', 'Packaging', 500)");
        db.execSQL("INSERT INTO supplies (name, category, stock) VALUES ('Coffee Straws / Stirrers', 'Supplies', 500)");
        db.execSQL("INSERT INTO supplies (name, category, stock) VALUES ('Takeout Paper Bags', 'Packaging', 100)");
    }

    public ArrayList<Product> getProducts(String category) {
        ArrayList<Product> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, category, hot, iced, stock " +
                "FROM products WHERE category = ?", new String[]{category});
        while (c.moveToNext()) {
            list.add(new Product(c.getInt(0), c.getString(1), c.getString(2),
                    c.getDouble(3), c.getDouble(4), c.getInt(5)));
        }
        c.close();
        return list;
    }

    public double[] getSalesBetween(String from, String to) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*), IFNULL(SUM(total),0) FROM orders " +
                "WHERE substr(date,1,10) BETWEEN ? AND ?", new String[]{from, to});
        c.moveToFirst();
        double[] result = {c.getInt(0), c.getDouble(1)};
        c.close();
        return result;
    }

    public int getLowStockCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM supplies WHERE stock <= ?",
                new String[]{String.valueOf(LOW_STOCK)});
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public String getBestSeller() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name, SUM(qty) as total_qty FROM order_items " +
                "GROUP BY name ORDER BY total_qty DESC LIMIT 1", null);
        String bestSeller = "None";
        if (c.moveToFirst()) {
            bestSeller = c.getString(0) + " (" + c.getInt(1) + " sold)";
        }
        c.close();
        return bestSeller;
    }

    public ArrayList<String[]> getRecentOrdersList(int limit) {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, date, total, order_type, payment_type FROM orders ORDER BY id DESC LIMIT ?",
                new String[]{String.valueOf(limit)});
        while (c.moveToNext()) {
            String id = "INV-" + String.format(Locale.US, "%06d", c.getInt(0));
            String date = c.getString(1);
            String total = "₱" + String.format(Locale.US, "%.0f", c.getDouble(2));
            String type = c.getString(3);
            if (type == null) type = "Dine-In";
            String payType = c.getString(4);
            if (payType == null) payType = "Cash";

            list.add(new String[]{id, date, total, type, payType});
        }
        c.close();
        return list;
    }

    public boolean setSupplyStock(int supplyId, int newStock) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("stock", newStock);
        int rows = db.update("supplies", v, "id = ?", new String[]{String.valueOf(supplyId)});
        return rows > 0;
    }

    public ArrayList<Product> getSuppliesAsProducts() {
        ArrayList<Product> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, category, stock FROM supplies", null);
        while (c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            String category = c.getString(2);
            int stock = c.getInt(3);
            list.add(new Product(id, name, category, 0, 0, stock));
        }
        c.close();
        return list;
    }

    public long saveOrder(ArrayList<CartItem> cart, double total, double cash, String orderType, String paymentType, String refNo) {
        SQLiteDatabase db = getWritableDatabase();

        int medioCupsNeeded = 0;
        int grandeCupsNeeded = 0;
        int totalSuppliesNeeded = 0;

        for (CartItem item : cart) {
            String itemNameLower = item.name.toLowerCase();

            boolean isAddOn = itemNameLower.contains("espresso shot") ||
                    itemNameLower.contains("oat milk") ||
                    itemNameLower.contains("sauce") ||
                    itemNameLower.contains("syrup") ||
                    itemNameLower.contains("cream");

            if (!isAddOn) {
                totalSuppliesNeeded += item.qty;
                if (itemNameLower.contains("grande") || itemNameLower.contains("large")) {
                    grandeCupsNeeded += item.qty;
                } else {
                    medioCupsNeeded += item.qty;
                }
            }

            Cursor cProd = db.rawQuery("SELECT stock FROM products WHERE id = ?", new String[]{String.valueOf(item.productId)});
            if (cProd.moveToFirst()) {
                int currentStock = cProd.getInt(0);
                if (item.qty > currentStock) {
                    lastError = "Error: Not enough stock for " + item.name + " (" + currentStock + " left)";
                    cProd.close();
                    return -1;
                }
            }
            cProd.close();
        }

        if (medioCupsNeeded > 0) {
            Cursor cSup = db.rawQuery("SELECT stock FROM supplies WHERE name LIKE '%Medio%'", null);
            if (cSup.moveToFirst() && cSup.getInt(0) < medioCupsNeeded) {
                lastError = "Error: Not enough Medio Paper Cups inventory!";
                cSup.close();
                return -1;
            }
            cSup.close();
        }

        if (grandeCupsNeeded > 0) {
            Cursor cSup = db.rawQuery("SELECT stock FROM supplies WHERE name LIKE '%Grande%'", null);
            if (cSup.moveToFirst() && cSup.getInt(0) < grandeCupsNeeded) {
                lastError = "Error: Not enough Grande Paper Cups inventory!";
                cSup.close();
                return -1;
            }
            cSup.close();
        }

        // --- CHECKING FOR TAKEOUT PAPER BAGS STOCK ---
        if (orderType != null && orderType.equalsIgnoreCase("Takeout")) {
            Cursor cBag = db.rawQuery("SELECT stock FROM supplies WHERE name LIKE '%Paper Bags%'", null);
            if (cBag.moveToFirst() && cBag.getInt(0) < 1) {
                lastError = "Error: Out of stock! No Takeout Paper Bags left.";
                cBag.close();
                return -1;
            }
            cBag.close();
        }

        db.beginTransaction();
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            ContentValues o = new ContentValues();
            o.put("date", date);
            o.put("total", total);
            o.put("cash", cash);
            o.put("change_amt", cash - total);
            o.put("order_type", orderType);
            o.put("payment_type", paymentType);
            o.put("ref_no", refNo);
            long orderId = db.insert("orders", null, o);

            for (CartItem item : cart) {
                ContentValues v = new ContentValues();
                v.put("order_id", orderId);
                v.put("name", item.name);
                v.put("qty", item.qty);
                v.put("price", item.price);
                v.put("product_id", item.productId);
                db.insert("order_items", null, v);

                db.execSQL("UPDATE products SET stock = stock - ? WHERE id = ?", new Object[]{item.qty, item.productId});
            }

            if (medioCupsNeeded > 0) {
                db.execSQL("UPDATE supplies SET stock = stock - ? WHERE name LIKE '%Medio%'",
                        new Object[]{medioCupsNeeded});
            }
            if (grandeCupsNeeded > 0) {
                db.execSQL("UPDATE supplies SET stock = stock - ? WHERE name LIKE '%Grande%'",
                        new Object[]{grandeCupsNeeded});
            }
            if (totalSuppliesNeeded > 0) {
                db.execSQL("UPDATE supplies SET stock = stock - ? WHERE name LIKE '%Lids%' OR name LIKE '%Straws%'",
                        new Object[]{totalSuppliesNeeded});
            }

            if (orderType != null && orderType.equalsIgnoreCase("Takeout")) {
                db.execSQL("UPDATE supplies SET stock = stock - 1 WHERE name LIKE '%Paper Bags%'");
            }

            db.setTransactionSuccessful();
            return orderId;
        } finally {
            db.endTransaction();
        }
    }

    public String getInvoiceHtml(long orderId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cOrder = db.rawQuery("SELECT date, total, cash, change_amt, order_type, payment_type, ref_no FROM orders WHERE id = ?",
                new String[]{String.valueOf(orderId)});

        if (!cOrder.moveToFirst()) {
            cOrder.close();
            return "<h3>Order not found</h3>";
        }

        String date = cOrder.getString(0);
        double total = cOrder.getDouble(1);
        double cash = cOrder.getDouble(2);
        double change = cOrder.getDouble(3);
        String orderType = cOrder.getString(4);
        if (orderType == null) orderType = "Dine-In";
        String paymentType = cOrder.getString(5);
        if (paymentType == null) paymentType = "Cash";
        String refNo = cOrder.getString(6);
        cOrder.close();

        Cursor cItemsCheck = db.rawQuery("SELECT SUM(qty * price) FROM order_items WHERE order_id = ?",
                new String[]{String.valueOf(orderId)});
        double subtotal = 0;
        if (cItemsCheck.moveToFirst()) {
            subtotal = cItemsCheck.getDouble(0);
        }
        cItemsCheck.close();

        boolean hasDiscount = total < subtotal;
        double discountAmount = subtotal - total;

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:monospace;'>");
        html.append("<h2 align='center'>KOFEIA COFFEE SHOP</h2>");
        html.append("<p align='center'>Order Type: <b>").append(orderType).append("</b><br>");
        html.append("Date: ").append(date).append("<br>");
        html.append("Order ID: INV-").append(String.format(Locale.US, "%06d", orderId)).append("</p>");
        html.append("<hr>");
        html.append("<table width='100%'>");
        html.append("<tr><th>Item</th><th>Qty</th><th>Price</th></tr>");

        Cursor cItems = db.rawQuery("SELECT name, qty, price FROM order_items WHERE order_id = ?",
                new String[]{String.valueOf(orderId)});
        while (cItems.moveToNext()) {
            String name = cItems.getString(0);
            int qty = cItems.getInt(1);
            double price = cItems.getDouble(2);
            html.append("<tr><td>").append(name).append("</td>")
                    .append("<td align='center'>").append(qty).append("</td>")
                    .append("<td align='right'>₱").append(String.format(Locale.US, "%.0f", price * qty)).append("</td></tr>");
        }
        cItems.close();

        html.append("</table>");
        html.append("<hr>");

        if (hasDiscount) {
            html.append("Subtotal: ₱").append(String.format(Locale.US, "%.0f", subtotal)).append("<br>");
            html.append("Senior/PWD Discount (20%): -₱").append(String.format(Locale.US, "%.0f", discountAmount)).append("<br>");
        }

        html.append("<p><b>Total to Pay: ₱").append(String.format(Locale.US, "%.0f", total)).append("</b><br>");
        html.append("Payment Method: <b>").append(paymentType).append("</b><br>");
        if (paymentType.equalsIgnoreCase("GCash / E-Wallet") && refNo != null && !refNo.isEmpty()) {
            html.append("Ref No: ").append(refNo).append("<br>");
        }
        html.append("Cash Received: ₱").append(String.format(Locale.US, "%.0f", cash)).append("<br>");
        html.append("Change: ₱").append(String.format(Locale.US, "%.0f", change)).append("</p>");
        html.append("<p align='center'>Thank you for ordering!</p>");
        html.append("</body></html>");

        return html.toString();
    }

    public String getReportHtml(String fromDate, String toDate, boolean byMonth) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:sans-serif; padding:10px;'>");
        html.append("<h2 align='center'>KOFEIA SALES REPORT</h2>");
        html.append("<p align='center'>From: ").append(fromDate).append(" To: ").append(toDate).append("</p>");
        html.append("<hr>");

        Cursor c;
        if (byMonth) {
            c = db.rawQuery("SELECT substr(date,1,7), COUNT(*), SUM(total) FROM orders " +
                            "WHERE substr(date,1,10) BETWEEN ? AND ? GROUP BY substr(date,1,7)",
                    new String[]{fromDate, toDate});
            html.append("<table width='100%' border='1' style='border-collapse:collapse; font-size:12px;'>");
            html.append("<tr bgcolor='#f2f2f2'><th>Month</th><th>Orders</th><th>Total Sales</th></tr>");
            double grandTotal = 0;
            while (c.moveToNext()) {
                html.append("<tr><td align='center'>").append(c.getString(0)).append("</td>")
                        .append("<td align='center'>").append(c.getInt(1)).append("</td>")
                        .append("<td align='right'>₱").append(String.format(Locale.US, "%,.0f", c.getDouble(2))).append("</td></tr>");
                grandTotal += c.getDouble(2);
            }
            html.append("</table>");
            html.append("<h3>Grand Total: ₱").append(String.format(Locale.US, "%,.0f", grandTotal)).append("</h3>");
        } else {
            c = db.rawQuery("SELECT id, date, total, order_type FROM orders " +
                            "WHERE substr(date,1,10) BETWEEN ? AND ? ORDER BY id DESC",
                    new String[]{fromDate, toDate});
            html.append("<table width='100%' border='1' style='border-collapse:collapse; font-size:12px;'>");
            html.append("<tr bgcolor='#f2f2f2'><th>ID</th><th>Date</th><th>Type</th><th>Total</th></tr>");
            double grandTotal = 0;
            while (c.moveToNext()) {
                String type = c.getString(3);
                if (type == null) type = "Dine-In";
                html.append("<tr><td align='center'>INV-").append(String.format(Locale.US, "%06d", c.getInt(0))).append("</td>")
                        .append("<td align='center'>").append(c.getString(1)).append("</td>")
                        .append("<td align='center'>").append(type).append("</td>")
                        .append("<td align='right'>₱").append(String.format(Locale.US, "%,.0f", c.getDouble(2))).append("</td></tr>");
                grandTotal += c.getDouble(2);
            }
            html.append("</table>");
            html.append("<h3>Grand Total: ₱").append(String.format(Locale.US, "%,.0f", grandTotal)).append("</h3>");
        }
        c.close();
        html.append("</body></html>");
        return html.toString();
    }
}