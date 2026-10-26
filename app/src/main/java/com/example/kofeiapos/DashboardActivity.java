package com.example.kofeiapos;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    DBHelper db;
    TextView tvDate, tvToday, tvTodayOrders, tvWeek, tvWeekOrders,
            tvMonth, tvMonthOrders, tvLow, tvBest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Ui.setup(this, findViewById(R.id.root));

        db = new DBHelper(this);

        tvDate = findViewById(R.id.tvDate);
        tvToday = findViewById(R.id.tvToday);
        tvTodayOrders = findViewById(R.id.tvTodayOrders);
        tvWeek = findViewById(R.id.tvWeek);
        tvWeekOrders = findViewById(R.id.tvWeekOrders);
        tvMonth = findViewById(R.id.tvMonth);
        tvMonthOrders = findViewById(R.id.tvMonthOrders);
        tvLow = findViewById(R.id.tvLow);
        tvBest = findViewById(R.id.tvBest);

        findViewById(R.id.btnPos).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.btnReports).setOnClickListener(v ->
                startActivity(new Intent(this, SalesActivity.class)));
        findViewById(R.id.btnInventory).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
        findViewById(R.id.btnInvoices).setOnClickListener(v -> showInvoices());
        findViewById(R.id.btnPassword).setOnClickListener(v -> showChangePassword());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    void loadStats() {
        String today = Ui.today();
        double[] d = db.getSalesBetween(today, today);
        double[] w = db.getSalesBetween(Ui.weekStart(), today);
        double[] m = db.getSalesBetween(Ui.monthStart(), today);

        tvDate.setText(new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(new Date()));

        tvToday.setText("₱" + money(d[1]));
        tvTodayOrders.setText((int) d[0] + (d[0] == 1 ? " order" : " orders"));
        tvWeek.setText("₱" + money(w[1]));
        tvWeekOrders.setText((int) w[0] + (w[0] == 1 ? " order" : " orders"));
        tvMonth.setText("₱" + money(m[1]));
        tvMonthOrders.setText((int) m[0] + (m[0] == 1 ? " order" : " orders"));

        int low = db.getLowStockCount();
        tvLow.setText(String.valueOf(low));
        tvLow.setTextColor(getResources().getColor(
                low > 0 ? R.color.kofeia_red : R.color.kofeia_green));

        tvBest.setText(db.getBestSeller());
    }

    void showInvoices() {
        ArrayList<String[]> orders = db.getRecentOrdersList(20);
        if (orders == null || orders.isEmpty()) {
            Toast.makeText(this, "No orders yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labels = new String[orders.size()];
        for (int i = 0; i < orders.size(); i++) {
            String[] o = orders.get(i);

            long orderId = 0;
            double amount = 0.0;
            String dateStr = o.length > 1 && o[1] != null ? o[1] : "";

            // Safe parsing for Order ID
            try {
                if (o.length > 0 && o[0] != null) {
                    String cleanId = o[0].replaceAll("[^0-9]", "");
                    if (!cleanId.isEmpty()) {
                        orderId = Long.parseLong(cleanId);
                    }
                }
            } catch (NumberFormatException e) {
                orderId = 0;
            }

            // Safe parsing for Total Amount (strips ₱, commas, spaces)
            try {
                if (o.length > 2 && o[2] != null) {
                    String cleanAmount = o[2].replaceAll("[^0-9.]", "").trim();
                    if (!cleanAmount.isEmpty()) {
                        amount = Double.parseDouble(cleanAmount);
                    }
                }
            } catch (NumberFormatException e) {
                amount = 0.0;
            }

            labels[i] = String.format(Locale.US, "INV-%06d   %s   ₱%s",
                    orderId, dateStr, money(amount));
        }

        new AlertDialog.Builder(this)
                .setTitle("Recent Invoices (tap to open)")
                .setItems(labels, (d, which) -> {
                    try {
                        String rawId = orders.get(which)[0];
                        String cleanId = rawId.replaceAll("[^0-9]", "");

                        if (cleanId.isEmpty()) {
                            Toast.makeText(this, "Invalid Order ID", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long orderId = Long.parseLong(cleanId);
                        Intent i = new Intent(this, InvoiceActivity.class);
                        i.putExtra("order_id", orderId);
                        startActivity(i);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error opening invoice", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    void showChangePassword() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(50, 20, 50, 0);

        EditText etOld = new EditText(this);
        etOld.setHint("Current password");
        etOld.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText etNew = new EditText(this);
        etNew.setHint("New password");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(etOld);
        box.addView(etNew);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(box)
                .setPositiveButton("Save", (dialog, w) -> {
                    String oldPass = etOld.getText().toString();
                    String newPass = etNew.getText().toString();
                    if (newPass.length() < 4) {
                        Toast.makeText(this, "New password must be at least 4 characters",
                                Toast.LENGTH_SHORT).show();
                    } else if (db.changePassword(oldPass, newPass)) {
                        Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Current password is wrong", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    void logout() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    String money(double value) {
        return String.format(Locale.US, "%,.0f", value);
    }
}