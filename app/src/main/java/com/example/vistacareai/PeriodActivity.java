package com.example.vistacareai;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PeriodActivity — Menstrual Cycle Tracker with AI-powered insights.
 *
 * LLM capability: Decision support / summarisation.
 * After predicting the next period, Llama 3.1 analyses the cycle history
 * and provides personalised wellness insights about cycle regularity.
 *
 * Privacy: Only date strings are sent to the API — no PII.
 * All period data is stored on-device only (SharedPreferences).
 */
public class PeriodActivity extends AppCompatActivity {

    private TextView txtMonthYear, txtSelectedDates, txtPrediction, txtHistory, txtAiInsight;
    private Button btnPrevMonth, btnNextMonth, btnSavePeriod, btnClearSelection, btnPredict;
    private GridLayout calendarGrid;
    private ProgressBar progressBar;
    private TextView txtLoading;

    private Calendar currentMonth;
    private ArrayList<String> selectedDates = new ArrayList<>();

    private SharedPreferences prefs;
    private static final String PREF_NAME = "PeriodPrefs";
    private static final String KEY_HISTORY = "history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

        txtMonthYear      = findViewById(R.id.txtMonthYear);
        txtSelectedDates  = findViewById(R.id.txtSelectedDates);
        txtPrediction     = findViewById(R.id.txtPrediction);
        txtHistory        = findViewById(R.id.txtHistory);
        txtAiInsight      = findViewById(R.id.txtAiInsight);
        btnPrevMonth      = findViewById(R.id.btnPrevMonth);
        btnNextMonth      = findViewById(R.id.btnNextMonth);
        btnSavePeriod     = findViewById(R.id.btnSavePeriod);
        btnClearSelection = findViewById(R.id.btnClearSelection);
        btnPredict        = findViewById(R.id.btnPredict);
        calendarGrid      = findViewById(R.id.calendarGrid);
        progressBar       = findViewById(R.id.progressPeriod);
        txtLoading        = findViewById(R.id.txtLoadingPeriod);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        renderCalendar();
        loadHistory();

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            renderCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            renderCalendar();
        });

        btnClearSelection.setOnClickListener(v -> {
            selectedDates.clear();
            txtSelectedDates.setText("Selected dates: none");
            txtPrediction.setText("");
            txtAiInsight.setText("");
            txtAiInsight.setVisibility(View.GONE);
            renderCalendar();
        });

        btnSavePeriod.setOnClickListener(v -> savePeriod());
        btnPredict.setOnClickListener(v -> predictNextPeriod());
    }

    private void renderCalendar() {
        calendarGrid.removeAllViews();

        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        txtMonthYear.setText(fmt.format(currentMonth.getTime()).toUpperCase());

        int firstDay    = currentMonth.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDay; i++) {
            TextView empty = new TextView(this);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width  = 0;
            p.height = dpToPx(48);
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            empty.setLayoutParams(p);
            calendarGrid.addView(empty);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            Calendar cal = (Calendar) currentMonth.clone();
            cal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = formatDate(cal);

            Button btn = new Button(this);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width      = 0;
            p.height     = dpToPx(48);
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            p.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(p);
            btn.setText(String.valueOf(day));
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(0, 0, 0, 0);

            if (selectedDates.contains(dateStr)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(0xFFFF80AB));
                btn.setTextColor(0xFFFFFFFF);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(0xFFDCE6FF));
                btn.setTextColor(0xFF1A2BE2);
            }

            btn.setOnClickListener(v -> {
                if (selectedDates.contains(dateStr)) {
                    selectedDates.remove(dateStr);
                } else {
                    selectedDates.add(dateStr);
                }
                updateSelectedText();
                renderCalendar();
            });

            calendarGrid.addView(btn);
        }
    }

    private void updateSelectedText() {
        if (selectedDates.isEmpty()) {
            txtSelectedDates.setText("Selected dates: none");
        } else {
            Collections.sort(selectedDates);
            txtSelectedDates.setText("Selected dates: " + selectedDates);
        }
    }

    private void savePeriod() {
        if (selectedDates.isEmpty()) {
            Toast.makeText(this, "Please select period dates first.", Toast.LENGTH_SHORT).show();
            return;
        }
        Collections.sort(selectedDates);
        Set<String> history = new HashSet<>(prefs.getStringSet(KEY_HISTORY, new HashSet<>()));
        history.add("Period: " + selectedDates.toString());
        prefs.edit().putStringSet(KEY_HISTORY, history).apply();
        Toast.makeText(this, "Period saved!", Toast.LENGTH_SHORT).show();
        loadHistory();
    }

    private void loadHistory() {
        Set<String> history = prefs.getStringSet(KEY_HISTORY, new HashSet<>());
        if (history.isEmpty()) {
            txtHistory.setText("No saved history yet.");
            return;
        }
        ArrayList<String> list = new ArrayList<>(history);
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        for (String item : list) sb.append("• ").append(item).append("\n\n");
        txtHistory.setText(sb.toString().trim());
    }

    private void predictNextPeriod() {
        Set<String> history = prefs.getStringSet(KEY_HISTORY, new HashSet<>());
        String startDate;

        if (!selectedDates.isEmpty()) {
            Collections.sort(selectedDates);
            startDate = selectedDates.get(0);
        } else if (!history.isEmpty()) {
            ArrayList<String> allDates = new ArrayList<>();
            for (String record : history) {
                String cleaned = record.replace("Period: [", "").replace("]", "");
                for (String d : cleaned.split(", "))
                    if (!d.trim().isEmpty()) allDates.add(d.trim());
            }
            if (allDates.isEmpty()) {
                txtPrediction.setText("No valid dates found.");
                return;
            }
            Collections.sort(allDates);
            startDate = allDates.get(allDates.size() - 1);
        } else {
            txtPrediction.setText("Please save or select a period first.");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            Calendar next = Calendar.getInstance();
            next.setTime(sdf.parse(startDate));
            next.add(Calendar.DAY_OF_MONTH, 28);
            String predictedDate = sdf.format(next.getTime());
            txtPrediction.setText("🔮 Next period predicted: " + predictedDate);

            // Get AI insight about cycle
            progressBar.setVisibility(View.VISIBLE);
            txtLoading.setVisibility(View.VISIBLE);
            btnPredict.setEnabled(false);

            String historyStr = history.isEmpty() ? "No prior history" : history.toString();
            String selectedStr = selectedDates.isEmpty() ? "None" : selectedDates.toString();

            LlamaApiHelper.getPeriodInsight(selectedStr, historyStr, predictedDate,
                    new LlamaApiHelper.LlamaCallback() {
                        @Override
                        public void onSuccess(String response) {
                            progressBar.setVisibility(View.GONE);
                            txtLoading.setVisibility(View.GONE);
                            btnPredict.setEnabled(true);
                            txtAiInsight.setText("🤖 AI Insight:\n\n" + response);
                            txtAiInsight.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            progressBar.setVisibility(View.GONE);
                            txtLoading.setVisibility(View.GONE);
                            btnPredict.setEnabled(true);
                            txtAiInsight.setText("🤖 AI Insight:\n\n" +
                                    "Your cycle prediction is based on a standard 28-day cycle. " +
                                    "Track regularly for more accurate predictions. " +
                                    "If you experience irregular cycles, consult a healthcare provider.\n\n" +
                                    "⚠ This is general wellness information only — not medical advice.\n\n" +
                                    "(Generated offline — AI was unavailable)");
                            txtAiInsight.setVisibility(View.VISIBLE);
                        }
                    });

        } catch (Exception e) {
            txtPrediction.setText("Prediction failed. Please try again.");
        }
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(cal.getTime());
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
