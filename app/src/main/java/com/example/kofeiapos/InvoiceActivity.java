package com.example.kofeiapos;

import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class InvoiceActivity extends AppCompatActivity {

    WebView webViewInvoice;
    long orderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_invoice);

            // 1. Ligtas na pagkuha ng order ID
            if (getIntent() != null) {
                orderId = getIntent().getLongExtra("order_id", -1);
            }

            // 2. I-init ang WebView
            webViewInvoice = findViewById(R.id.webViewInvoice);

            if (webViewInvoice != null) {
                String htmlContent = "<h3>Loading invoice...</h3>";

                if (orderId != -1) {
                    try {
                        DBHelper db = new DBHelper(InvoiceActivity.this);
                        String dbHtml = db.getInvoiceHtml(orderId);
                        if (dbHtml != null && !dbHtml.isEmpty()) {
                            // Dinagdagan ng extra space sa ilalim para hindi matakpan ng buttons
                            if (dbHtml.contains("</body>")) {
                                htmlContent = dbHtml.replace("</body>", "<div style=\"height: 120px;\"></div></body>");
                            } else {
                                htmlContent = dbHtml + "<div style=\"height: 250px;\"></div>";
                            }
                        } else {
                            htmlContent = "<h3>Error: Invoice data is empty.</h3>";
                        }
                    } catch (Exception dbEx) {
                        htmlContent = "<h3>Database Error: " + dbEx.getMessage() + "</h3>";
                    }
                } else {
                    htmlContent = "<h3>Error: Invalid Order ID (" + orderId + ")</h3>";
                }

                // I-load sa WebView nang ligtas
                webViewInvoice.getSettings().setJavaScriptEnabled(true);
                webViewInvoice.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            }

            // 3. Button para sa DONE
            TextView btnDone = findViewById(R.id.btnDone);
            if (btnDone != null) {
                btnDone.setOnClickListener(v -> finish());
            }

            // 4. Button para sa PRINT / SAVE AS PDF
            TextView btnPrint = findViewById(R.id.btnPrint);
            if (btnPrint != null) {
                btnPrint.setOnClickListener(v -> printInvoice());
            }

        } catch (Exception e) {
            Toast.makeText(InvoiceActivity.this, "Invoice Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    void printInvoice() {
        if (webViewInvoice != null) {
            try {
                PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                if (pm != null) {
                    PrintDocumentAdapter adapter =
                            webViewInvoice.createPrintDocumentAdapter("Kofeia_Invoice_" + orderId);
                    pm.print("Kofeia Invoice", adapter, new PrintAttributes.Builder().build());
                }
            } catch (Exception e) {
                Toast.makeText(InvoiceActivity.this, "Printing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}