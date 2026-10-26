package com.example.kofeiapos;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    DBHelper db;
    LinearLayout llCategories;
    GridView gvProducts;
    ListView lvCart;
    TextView tvTotal, tvItemCount, tvEmpty, btnCheckout, btnClear, btnDashboard;

    String[] categories = {"Espresso", "Non-Coffee", "Matcha", "Refreshment",
            "Signature", "Add Ons"};

    int currentCategory = 0;
    ArrayList<TextView> chips = new ArrayList<>();
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<CartItem> cart = new ArrayList<>();

    ProductAdapter productAdapter;
    CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Ui.setup(this, findViewById(R.id.root));

        db = new DBHelper(this);

        llCategories = findViewById(R.id.llCategories);
        gvProducts = findViewById(R.id.gvProducts);
        lvCart = findViewById(R.id.lvCart);
        tvTotal = findViewById(R.id.tvTotal);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnClear = findViewById(R.id.btnClear);
        btnDashboard = findViewById(R.id.btnSales);

        productAdapter = new ProductAdapter();
        cartAdapter = new CartAdapter();
        gvProducts.setAdapter(productAdapter);
        lvCart.setAdapter(cartAdapter);
        lvCart.setEmptyView(tvEmpty);

        buildCategoryChips();

        gvProducts.setOnItemClickListener((parent, view, pos, id) ->
                onProductTapped(products.get(pos)));

        btnCheckout.setOnClickListener(v -> showPaymentDialog());
        btnClear.setOnClickListener(v -> { cart.clear(); refreshCart(); });
        btnDashboard.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productAdapter != null) loadProducts(categories[currentCategory]);
    }

    void buildCategoryChips() {
        llCategories.removeAllViews();
        chips.clear();
        for (int i = 0; i < categories.length; i++) {
            TextView chip = new TextView(this);
            chip.setText(categories[i]);
            chip.setTextSize(14);
            chip.setTypeface(null, Typeface.BOLD);
            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setPadding(dp(18), dp(8), dp(18), dp(8));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(8), 0);
            chip.setLayoutParams(lp);

            final int index = i;
            chip.setOnClickListener(v -> selectCategory(index));

            llCategories.addView(chip);
            chips.add(chip);
        }
        selectCategory(0);
    }

    void selectCategory(int index) {
        currentCategory = index;
        for (int i = 0; i < chips.size(); i++) {
            boolean selected = (i == index);
            chips.get(i).setSelected(selected);
            chips.get(i).setTextColor(getResources().getColor(
                    selected ? R.color.white : R.color.kofeia_green));
        }
        loadProducts(categories[index]);
    }

    void loadProducts(String category) {
        products.clear();
        products.addAll(db.getProducts(category));
        productAdapter.notifyDataSetChanged();
    }

    String priceText(Product p) {
        if (p.hot > 0 && p.iced > 0) {
            if (p.hot == p.iced) return "₱" + money(p.hot);
            return "₱" + money(p.hot) + " / ₱" + money(p.iced);
        }
        if (p.hot > 0) return "₱" + money(p.hot);
        return "₱" + money(p.iced);
    }

    String tagText(Product p) {
        if (p.stock <= 0) return "SOLD OUT";
        String stock = p.stock + " left";
        if (p.category.equals("Add Ons")) return stock;
        if (p.hot > 0 && p.iced > 0) return "Medio / Grande • " + stock;
        if (p.hot > 0) return "Medio only • " + stock;
        return "Grande only • " + stock;
    }

    // Helper method para i-check ang actual supply stock ng Paper Cups sa database
    int getCupSupplyStock(String size) {
        SQLiteDatabase readableDb = db.getReadableDatabase();
        Cursor c = readableDb.rawQuery("SELECT stock FROM supplies WHERE name LIKE ?", new String[]{"%" + size + "%"});
        int stock = 999;
        if (c.moveToFirst()) {
            stock = c.getInt(0);
        }
        c.close();
        return stock;
    }

    // Helper method para i-check ang stock ng Takeout Paper Bags
    int getTakeoutBagStock() {
        SQLiteDatabase readableDb = db.getReadableDatabase();
        Cursor c = readableDb.rawQuery("SELECT stock FROM supplies WHERE name LIKE '%Paper Bags%'", null);
        int stock = 0;
        if (c.moveToFirst()) {
            stock = c.getInt(0);
        }
        c.close();
        return stock;
    }

    // Helper method para bilangan kung ilang cup ng napiling size ang naroon na sa cart
    int getCupCountInCart(String size) {
        int count = 0;
        for (CartItem item : cart) {
            if (item.name.toLowerCase().contains(size.toLowerCase())) {
                count += item.qty;
            }
        }
        return count;
    }

    void onProductTapped(Product p) {
        if (p.stock <= 0) {
            Toast.makeText(this, p.name + " is SOLD OUT!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isAddOn = p.category.equalsIgnoreCase("Add Ons");

        if (isAddOn) {
            // Pag Add-On, direct add kagad
            addToCart(p, p.name, p.hot > 0 ? p.hot : p.iced);
        } else if (p.hot > 0 && p.iced > 0) {
            String[] choices = {"Medio  ₱" + money(p.hot), "Grande  ₱" + money(p.iced)};
            new AlertDialog.Builder(this)
                    .setTitle(p.name + " (Stock: " + p.stock + " left)")
                    .setItems(choices, (d, which) -> {
                        String selectedSize = (which == 0) ? "Medio" : "Grande";
                        int availableCups = getCupSupplyStock(selectedSize);
                        int cupsInCart = getCupCountInCart(selectedSize);

                        if (cupsInCart + 1 > availableCups) {
                            Toast.makeText(this, "OUT OF STOCK: No available " + selectedSize + " cups left!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (which == 0) {
                            addToCart(p, p.name + " (Medio)", p.hot);
                        } else {
                            addToCart(p, p.name + " (Grande)", p.iced);
                        }
                    })
                    .show();
        } else if (p.hot > 0) {
            int availableCups = getCupSupplyStock("Medio");
            int cupsInCart = getCupCountInCart("Medio");
            if (cupsInCart + 1 > availableCups) {
                Toast.makeText(this, "OUT OF STOCK: No available Medio cups left!", Toast.LENGTH_LONG).show();
                return;
            }
            addToCart(p, p.name + " (Medio)", p.hot);
        } else {
            int availableCups = getCupSupplyStock("Grande");
            int cupsInCart = getCupCountInCart("Grande");
            if (cupsInCart + 1 > availableCups) {
                Toast.makeText(this, "OUT OF STOCK: No available Grande cups left!", Toast.LENGTH_LONG).show();
                return;
            }
            addToCart(p, p.name + " (Grande)", p.iced);
        }
    }

    int qtyInCart(int productId) {
        int n = 0;
        for (CartItem c : cart) if (c.productId == productId) n += c.qty;
        return n;
    }

    void addToCart(Product p, String name, double price) {
        int currentQtyInCart = qtyInCart(p.id);
        if (currentQtyInCart >= p.stock) {
            Toast.makeText(this, "Stock limit reached! Only " + p.stock + " left.", Toast.LENGTH_SHORT).show();
            return;
        }
        for (CartItem c : cart) {
            if (c.name.equals(name)) {
                if (c.qty + 1 > p.stock) {
                    Toast.makeText(this, "Only " + p.stock + " left in stock!", Toast.LENGTH_SHORT).show();
                    return;
                }
                c.qty++;
                refreshCart();
                return;
            }
        }
        cart.add(new CartItem(p.id, name, price, 1, p.stock));
        refreshCart();
    }

    void refreshCart() {
        cartAdapter.notifyDataSetChanged();
        int count = 0;
        for (CartItem c : cart) count += c.qty;
        tvItemCount.setText(count + (count == 1 ? " item" : " items"));
        tvTotal.setText("₱" + money(getTotal()));
    }

    double getTotal() {
        double total = 0;
        for (CartItem c : cart) total += c.price * c.qty;
        return total;
    }

    void showPaymentDialog() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Order is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        final double originalTotal = getTotal();

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(50, 20, 50, 0);

        CheckBox cbDiscount = new CheckBox(this);
        cbDiscount.setText("Apply Senior / PWD Discount (20%)");
        box.addView(cbDiscount);

        TextView tvCalcTotal = new TextView(this);
        tvCalcTotal.setText("Total to pay: ₱" + money(originalTotal));
        tvCalcTotal.setPadding(0, 20, 0, 20);
        tvCalcTotal.setTextSize(16);
        box.addView(tvCalcTotal);

        cbDiscount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            double finalToPay = isChecked ? originalTotal * 0.80 : originalTotal;
            tvCalcTotal.setText("Total to pay: ₱" + money(finalToPay) + (isChecked ? " (With 20% Discount)" : ""));
        });

        new AlertDialog.Builder(this)
                .setTitle("Select Payment Method")
                .setView(box)
                .setPositiveButton("Cash", (d, w) -> {
                    double finalTotal = cbDiscount.isChecked() ? originalTotal * 0.80 : originalTotal;
                    showCashDialog(finalTotal);
                })
                .setNeutralButton("GCash / E-Wallet", (d, w) -> {
                    double finalTotal = cbDiscount.isChecked() ? originalTotal * 0.80 : originalTotal;
                    showOnlinePaymentDialog(finalTotal);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    void showCashDialog(double total) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(50, 20, 50, 0);

        EditText et = new EditText(this);
        et.setHint("Cash received");
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        box.addView(et, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(this)
                .setTitle("Cash Payment")
                .setMessage("Total Amount: ₱" + money(total))
                .setView(box)
                .setPositiveButton("Confirm", (d, w) -> {
                    try {
                        double cash = Double.parseDouble(et.getText().toString().trim());
                        promptOrderTypeAndPay(total, cash, "Cash", "");
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter valid cash amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Exact Amount", (d, w) -> promptOrderTypeAndPay(total, total, "Cash", ""))
                .setNegativeButton("Back", (d, w) -> showPaymentDialog())
                .show();
    }

    void showOnlinePaymentDialog(double total) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(50, 20, 50, 0);

        TextView tvInstruction = new TextView(this);
        tvInstruction.setText("Scan QR to pay ₱" + money(total) + " via GCash / Maya");
        tvInstruction.setPadding(0, 0, 0, 15);
        box.addView(tvInstruction);

        ImageView ivQrCode = new ImageView(this);
        ivQrCode.setImageResource(R.drawable.qrcode);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(500, 500);
        imgParams.gravity = android.view.Gravity.CENTER;
        ivQrCode.setLayoutParams(imgParams);
        ivQrCode.setScaleType(ImageView.ScaleType.FIT_CENTER);
        box.addView(ivQrCode);

        EditText etRefNumber = new EditText(this);
        etRefNumber.setHint("Enter Reference No.");
        etRefNumber.setPadding(0, 20, 0, 0);
        box.addView(etRefNumber);

        new AlertDialog.Builder(this)
                .setTitle("Online Payment Demo")
                .setView(box)
                .setPositiveButton("Verify & Confirm", (d, w) -> {
                    String ref = etRefNumber.getText().toString().trim();
                    if (ref.isEmpty()) {
                        Toast.makeText(this, "Please enter reference number", Toast.LENGTH_SHORT).show();
                        showOnlinePaymentDialog(total);
                    } else {
                        Toast.makeText(this, "Payment Verified!", Toast.LENGTH_SHORT).show();
                        promptOrderTypeAndPay(total, total, "GCash / E-Wallet", ref);
                    }
                })
                .setNegativeButton("Back", (d, w) -> showPaymentDialog())
                .show();
    }

    void promptOrderTypeAndPay(double total, double cash, String paymentType, String refNo) {
        if (cash < total) {
            Toast.makeText(this, "Not enough cash", Toast.LENGTH_SHORT).show();
            showPaymentDialog();
            return;
        }

        String[] options = {"Dine-In", "Takeout"};
        new AlertDialog.Builder(this)
                .setTitle("Select Order Type")
                .setItems(options, (dialog, which) -> {
                    String selectedType = options[which];

                    // --- CHECKING FOR TAKEOUT BAG STOCK ---
                    if (selectedType.equalsIgnoreCase("Takeout")) {
                        int bagStock = getTakeoutBagStock();
                        if (bagStock <= 0) {
                            Toast.makeText(this, "OUT OF STOCK: Takeout Paper Bags!", Toast.LENGTH_LONG).show();
                            promptOrderTypeAndPay(total, cash, paymentType, refNo);
                            return; // Hindi tutuloy sa pay()
                        }
                    }

                    pay(total, cash, selectedType, paymentType, refNo);
                })
                .setCancelable(false)
                .show();
    }

    void pay(double total, double cash, String orderType, String paymentType, String refNo) {
        long orderId = db.saveOrder(cart, total, cash, orderType, paymentType, refNo);
        if (orderId < 0) {
            Toast.makeText(this, db.lastError, Toast.LENGTH_LONG).show();
            return;
        }

        cart.clear();
        refreshCart();
        loadProducts(categories[currentCategory]);

        Intent i = new Intent(this, InvoiceActivity.class);
        i.putExtra("order_id", orderId);
        startActivity(i);
    }

    class ProductAdapter extends ArrayAdapter<Product> {
        ProductAdapter() {
            super(MainActivity.this, 0, products);
        }

        @NonNull
        @Override
        public View getView(int pos, @Nullable View convertView,
                            @NonNull ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                v = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_product, parent, false);
            }

            Product p = getItem(pos);

            ImageView image = v.findViewById(R.id.ivProductImage);
            TextView name = v.findViewById(R.id.tvName);
            TextView price = v.findViewById(R.id.tvPrice);
            TextView tag = v.findViewById(R.id.tvTag);

            name.setText(p.name);
            price.setText(priceText(p));
            tag.setText(tagText(p));

            tag.setTextColor(getResources().getColor(
                    p.stock <= DBHelper.LOW_STOCK
                            ? R.color.kofeia_red
                            : R.color.kofeia_gray));

            image.setImageResource(getProductImage(p.name));

            v.setAlpha(p.stock <= 0 ? 0.45f : 1f);

            return v;
        }
    }

    int getProductImage(String productName) {
        String name = productName.toLowerCase(Locale.ROOT);

        if (name.contains("dirty ube tresleches")) return R.drawable.dirty_ube_tresleches;
        if (name.contains("pistachio espresso")) return R.drawable.pistachio_espresso;
        if (name.contains("biscoff espresso")) return R.drawable.biscoff_espresso;
        if (name.contains("sea salt spanish latte")) return R.drawable.sea_salt_spanish_latte;
        if (name.contains("sea salt caramel latte")) return R.drawable.sea_salt_caramel_latte;
        if (name.contains("caramel macchiato")) return R.drawable.caramel_macchiato;
        if (name.contains("cinnamon roll latte")) return R.drawable.cinnamon_roll_latte;
        if (name.contains("butterscotch latte")) return R.drawable.butterscotch_latte;
        if (name.contains("white mocha")) return R.drawable.white_mocha;
        if (name.contains("mocha")) return R.drawable.mocha;
        if (name.contains("vanilla latte")) return R.drawable.vanilla_latte;
        if (name.contains("spanish latte")) return R.drawable.spanish_latte;
        if (name.contains("cafe latte")) return R.drawable.cafe_latte;
        if (name.contains("americano")) return R.drawable.americano;

        if (name.contains("oreo choco cloud")) return R.drawable.oreo_choco_cloud;
        if (name.contains("biscoff cloud")) return R.drawable.biscoff_cloud;
        if (name.contains("strawberry tresleches")) return R.drawable.strawberry_tresleches;
        if (name.contains("ube tresleches")) return R.drawable.ube_tresleches;
        if (name.contains("caramel pistachio milk")) return R.drawable.caramel_pistachio_milk;
        if (name.contains("strawberry chocolate")) return R.drawable.strawberry_chocolate;

        if (name.contains("sea salt matcha biscoff")) return R.drawable.sea_salt_matcha_biscoff;
        if (name.contains("matcha pistachio chocolate")) return R.drawable.matcha_pistachio_chocolate;
        if (name.contains("matcha pistachio")) return R.drawable.matcha_pistachio;
        if (name.contains("matcha biscoff")) return R.drawable.matcha_biscoff;
        if (name.contains("strawberry matcha")) return R.drawable.strawberry_matcha;
        if (name.contains("mango matcha")) return R.drawable.mango_matcha;
        if (name.contains("ube matcha latte")) return R.drawable.ube_matcha_latte;
        if (name.contains("sea salt matcha latte")) return R.drawable.sea_salt_matcha_latte;
        if (name.contains("dirty matcha")) return R.drawable.dirty_matcha;
        if (name.contains("matcha latte")) return R.drawable.matcha_latte;

        if (name.contains("sparkleberry")) return R.drawable.sparkleberry;
        if (name.contains("strawberry/mango ade") || name.contains("strawberry mango ade") || name.contains("ade"))
            return R.drawable.strawberry_mango_ade;
        if (name.contains("honey citron & ginger tea")) return R.drawable.honey_citron_ginger_tea;
        if (name.contains("honey citron tea")) return R.drawable.honey_citron_tea;

        if (name.contains("kofeia hot choco")) return R.drawable.kofeia_hot_choco;

        if (name.contains("espresso shot")) return R.drawable.espresso_shot;
        if (name.contains("oat milk")) return R.drawable.oat_milk;
        if (name.contains("sauce") || name.contains("syrup")) return R.drawable.sauce_syrup;

        return R.drawable.ic_launcher_foreground;
    }

    class CartAdapter extends ArrayAdapter<CartItem> {
        CartAdapter() {
            super(MainActivity.this, 0, cart);
        }

        @NonNull
        @Override
        public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.item_cart, parent, false);
            }
            CartItem c = getItem(pos);
            ((TextView) v.findViewById(R.id.tvCartName)).setText(c.name);
            ((TextView) v.findViewById(R.id.tvCartPrice)).setText("₱" + money(c.price * c.qty));
            ((TextView) v.findViewById(R.id.tvQty)).setText(String.valueOf(c.qty));

            v.findViewById(R.id.btnPlus).setOnClickListener(x -> {
                boolean isAddOn = c.name.toLowerCase().contains("espresso shot") ||
                        c.name.toLowerCase().contains("oat milk") ||
                        c.name.toLowerCase().contains("sauce") ||
                        c.name.toLowerCase().contains("syrup") ||
                        c.name.toLowerCase().contains("cream");

                if (!isAddOn) {
                    String size = c.name.toLowerCase().contains("grande") ? "Grande" : "Medio";
                    int availableCups = getCupSupplyStock(size);
                    int cupsInCart = getCupCountInCart(size);

                    if (cupsInCart + 1 > availableCups) {
                        Toast.makeText(MainActivity.this, "Cannot add more. No available " + size + " cups left!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (c.qty >= c.stock) {
                    Toast.makeText(MainActivity.this, "Cannot add more. Only " + c.stock + " left in stock.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                c.qty++;
                refreshCart();
            });
            v.findViewById(R.id.btnMinus).setOnClickListener(x -> {
                c.qty--;
                if (c.qty <= 0) cart.remove(c);
                refreshCart();
            });
            return v;
        }
    }

    int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    String money(double value) {
        return String.format(Locale.US, "%.0f", value);
    }
}