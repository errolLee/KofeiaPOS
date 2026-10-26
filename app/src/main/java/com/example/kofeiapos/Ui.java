package com.example.kofeiapos;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Ui {

    // Call right after setContentView(). Root view needs android:id="@+id/root"
    public static void setup(Activity activity, View root) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), root);
        controller.setAppearanceLightStatusBars(false);       // white clock + signal icons
        controller.setAppearanceLightNavigationBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());          // keyboard too
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    // ---------- DATE HELPERS (format: 2026-10-17) ----------
    private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String today() {
        return FMT.format(Calendar.getInstance().getTime());
    }

    public static String weekStart() {            // Monday of this week
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return FMT.format(c.getTime());
    }

    public static String monthStart() {           // 1st day of this month
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        return FMT.format(c.getTime());
    }

    public static String sixMonthsStart() {       // 1st day of the month, 5 months back
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, -5);
        return FMT.format(c.getTime());
    }

    public static String yearStart() {            // January 1
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, 1);
        return FMT.format(c.getTime());
    }
}
