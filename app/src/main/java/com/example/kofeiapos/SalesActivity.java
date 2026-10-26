package com.example.kofeiapos;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class SalesActivity extends AppCompatActivity {

    DBHelper db;
    TextView[] chips;               // Today, Week, Month, 6 Mos, Year
    TextView btnFrom, btnTo, btnPrint;
    WebView webView;

    String fromDate, toDate;        // format: 2026-10-07
    boolean byMonth = false;        // 6 Mos and Year = breakdown per month

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        Ui.setup(this, findViewById(R.id.root));

        db = new DBHelper(this);
        chips = new TextView[]{
                findViewById(R.id.chipToday), findViewById(R.id.chipWeek),
                findViewById(R.id.chipMonth), findViewById(R.id.chip6Months),
                findViewById(R.id.chipYear)};
        btnFrom = findViewById(R.id.btnFrom);
        btnTo = findViewById(R.id.btnTo);
        btnPrint = findViewById(R.id.btnPrint);
        webView = findViewById(R.id.webView);

        for (int i = 0; i < chips.length; i++) {
            final int index = i;
            chips[i].setOnClickListener(v -> selectPeriod(index));
        }
        btnFrom.setOnClickListener(v -> pickDate(true));
        btnTo.setOnClickListener(v -> pickDate(false));
        btnPrint.setOnClickListener(v -> printReport());

        selectPeriod(0);   // start with Today
    }

    // 0 = Today, 1 = Week, 2 = Month, 3 = 6 Months, 4 = Year
    void selectPeriod(int p) {
        toDate = Ui.today();
        byMonth = false;

        if (p == 0) {
            fromDate = toDate;
        } else if (p == 1) {
            fromDate = Ui.weekStart();
        } else if (p == 2) {
            fromDate = Ui.monthStart();
        } else if (p == 3) {
            fromDate = Ui.sixMonthsStart();
            byMonth = true;
        } else {
            fromDate = Ui.yearStart();
            byMonth = true;
        }
        highlightChip(p);
        updateButtons();
        showReport();
    }

    // index -1 = no chip selected (custom dates)
    void highlightChip(int index) {
        for (int i = 0; i < chips.length; i++) {
            boolean selected = (i == index);
            chips[i].setSelected(selected);
            chips[i].setTextColor(getResources().getColor(
                    selected ? R.color.white : R.color.kofeia_green));
        }
    }

    void pickDate(boolean isFrom) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            if (isFrom) fromDate = date;
            else toDate = date;
            byMonth = false;
            highlightChip(-1);   // custom
            updateButtons();
            showReport();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    void updateButtons() {
        btnFrom.setText("From: " + fromDate);
        btnTo.setText("To: " + toDate);
    }

    void showReport() {
        if (fromDate.compareTo(toDate) > 0) {
            Toast.makeText(this, "'From' date must be before 'To' date", Toast.LENGTH_SHORT).show();
            return;
        }
        webView.loadDataWithBaseURL(null, db.getReportHtml(fromDate, toDate, byMonth),
                "text/html", "UTF-8", null);
    }

    // Opens Android's print screen (print, or "Save as PDF")
    void printReport() {
        PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter adapter =
                webView.createPrintDocumentAdapter("Kofeia_Sales_" + fromDate + "_to_" + toDate);
        pm.print("Kofeia Sales Report", adapter, new PrintAttributes.Builder().build());
    }
}
