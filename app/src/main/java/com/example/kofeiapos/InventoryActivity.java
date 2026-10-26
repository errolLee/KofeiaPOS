package com.example.kofeiapos;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    DBHelper db;
    ListView lvStock;
    TextView tvSummary;
    ArrayList<Product> list = new ArrayList<>();
    StockAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Ui.setup(this, findViewById(R.id.root));

        db = new DBHelper(this);
        lvStock = findViewById(R.id.lvStock);
        tvSummary = findViewById(R.id.tvSummary);

        adapter = new StockAdapter();
        lvStock.setAdapter(adapter);
        lvStock.setOnItemClickListener((parent, view, pos, id) -> showEditDialog(list.get(pos)));

        load();
    }

    void load() {
        list.clear();
        // In-update para kunin ang supplies sa halip na buong menu products
        list.addAll(db.getSuppliesAsProducts());
        adapter.notifyDataSetChanged();

        int low = 0;
        for (Product p : list) if (p.stock <= DBHelper.LOW_STOCK) low++;
        tvSummary.setText(list.size() + " supplies  •  " + low + " low / out of stock   (tap to edit)");
    }

    // Admin types the new stock number
    void showEditDialog(Product p) {
        LinearLayout box = new LinearLayout(this);
        box.setPadding(50, 20, 50, 0);

        EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(p.stock));
        et.setSelection(et.getText().length());
        box.addView(et, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(this)
                .setTitle(p.name)
                .setMessage("Type the new total stock:")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String text = et.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, "Enter a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // In-update para i-save ang stock sa supplies table
                    db.setSupplyStock(p.id, Integer.parseInt(text));
                    load();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    class StockAdapter extends ArrayAdapter<Product> {
        StockAdapter() {
            super(InventoryActivity.this, 0, list);
        }

        @NonNull
        @Override
        public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.item_stock, parent, false);
            }
            Product p = getItem(pos);
            TextView qty = v.findViewById(R.id.tvStockQty);

            ((TextView) v.findViewById(R.id.tvStockName)).setText(p.name);
            ((TextView) v.findViewById(R.id.tvStockCat)).setText(p.category);

            if (p.stock <= 0) qty.setText("OUT");
            else qty.setText(String.valueOf(p.stock));

            qty.setTextColor(getResources().getColor(
                    p.stock <= DBHelper.LOW_STOCK ? R.color.kofeia_red : R.color.kofeia_green));
            return v;
        }
    }
}