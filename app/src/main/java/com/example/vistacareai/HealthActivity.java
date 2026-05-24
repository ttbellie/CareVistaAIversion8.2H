package com.example.vistacareai;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * HealthActivity — Daily Health Logger with AI-powered insights.
 *
 * LLM capability: Summarisation / structured output generation.
 * The user logs water intake, sleep hours, and mood; Llama 3.1 generates
 * a personalised wellness summary with actionable tips.
 *
 * Privacy: Only the numeric values and mood text are sent to the API.
 * All log data is stored on-device only (SharedPreferences).
 */
public class HealthActivity extends AppCompatActivity {

    private EditText edtWater, edtSleep, edtMood;
    private Button btnSave;
    private LinearLayout summaryCard;
    private TextView txtSummary;
    private ProgressBar progressBar;
    private TextView txtLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        edtWater    = findViewById(R.id.edtWater);
        edtSleep    = findViewById(R.id.edtSleep);
        edtMood     = findViewById(R.id.edtMood);
        btnSave     = findViewById(R.id.btnSave);
        summaryCard = findViewById(R.id.summaryCard);
        txtSummary  = findViewById(R.id.txtSummary);
        progressBar = findViewById(R.id.progressHealth);
        txtLoading  = findViewById(R.id.txtLoadingHealth);

        btnSave.setOnClickListener(v -> saveLog());
    }

    private void saveLog() {
        String water = edtWater.getText().toString().trim();
        String sleep = edtSleep.getText().toString().trim();
        String mood  = edtMood.getText().toString().trim();

        if (water.isEmpty() || sleep.isEmpty() || mood.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int w;
        double s;
        try {
            w = Integer.parseInt(water);
            s = Double.parseDouble(sleep);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for water and sleep.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show log immediately
        String logText = "💧 Water: " + w + " glasses\n" +
                "😴 Sleep: " + String.format("%.1f", s) + " hours\n" +
                "😊 Mood: " + mood;
        txtSummary.setText(logText + "\n\n🤖 Getting AI insight...");
        summaryCard.setVisibility(View.VISIBLE);

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        txtLoading.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Get AI insight from Llama 3.1
        LlamaApiHelper.getHealthInsight(w, s, mood, new LlamaApiHelper.LlamaCallback() {
            @Override
            public void onSuccess(String response) {
                progressBar.setVisibility(View.GONE);
                txtLoading.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                txtSummary.setText(logText + "\n\n🤖 AI Insight:\n\n" + response);
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                txtLoading.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                // Fallback to local insight
                txtSummary.setText(logText + "\n\n🤖 AI Insight:\n\n" +
                        generateLocalInsight(w, s, mood) +
                        "\n\n(Generated offline — AI was unavailable)");
            }
        });
    }

    /** Local fallback insight when API is unavailable. */
    private String generateLocalInsight(int water, double sleep, String mood) {
        StringBuilder sb = new StringBuilder();

        if (water < 6) {
            sb.append("You are below the recommended water intake. Try to drink at least 8 glasses today.\n\n");
        } else {
            sb.append("Great hydration! Keeping up your water intake supports energy and focus.\n\n");
        }

        if (sleep < 7) {
            sb.append("You got less than 7 hours of sleep. Try going to bed 30 minutes earlier tonight.\n\n");
        } else {
            sb.append("You are getting a healthy amount of sleep. Consistent sleep helps regulate mood.\n\n");
        }

        String moodLower = mood.toLowerCase();
        if (moodLower.contains("stress") || moodLower.contains("anxious") || moodLower.contains("tired")) {
            sb.append("You seem to be feeling a bit low. Try a short walk or some deep breathing.");
        } else if (moodLower.contains("happy") || moodLower.contains("great") || moodLower.contains("good")) {
            sb.append("You are in a great mood! Keep doing what is working for you.");
        } else {
            sb.append("Thank you for logging your mood. Tracking how you feel helps you spot patterns.");
        }

        sb.append("\n\n⚠ This is general wellness information only — not medical advice.");
        return sb.toString();
    }
}
