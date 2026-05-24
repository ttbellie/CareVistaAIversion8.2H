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
 * BmiActivity — BMI Calculator with AI-powered personalised advice.
 *
 * LLM capability: Structured output generation (personalised health advice).
 * The BMI calculation is done locally; only the result is sent to Llama 3.1
 * for generating personalised, context-aware wellness advice.
 *
 * Privacy: Only the BMI value and category are sent to the API — no PII.
 */
public class BmiActivity extends AppCompatActivity {

    private EditText edtHeight, edtWeight;
    private Button btnCalculate;
    private LinearLayout resultCard;
    private TextView txtBmiValue, txtCategory, txtAdvice;
    private ProgressBar progressBar;
    private TextView txtLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        edtHeight    = findViewById(R.id.edtHeight);
        edtWeight    = findViewById(R.id.edtWeight);
        btnCalculate = findViewById(R.id.btnCalculate);
        resultCard   = findViewById(R.id.resultCard);
        txtBmiValue  = findViewById(R.id.txtBmiValue);
        txtCategory  = findViewById(R.id.txtCategory);
        txtAdvice    = findViewById(R.id.txtAdvice);
        progressBar  = findViewById(R.id.progressBmi);
        txtLoading   = findViewById(R.id.txtLoadingBmi);

        btnCalculate.setOnClickListener(v -> calculateBMI());
    }

    private void calculateBMI() {
        String heightStr = edtHeight.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter height and weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        double h, w;
        try {
            h = Double.parseDouble(heightStr);
            w = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (h <= 0 || w <= 0) {
            Toast.makeText(this, "Values must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        double bmi = w / (h * h);
        String category = getCategory(bmi);

        // Show BMI result immediately
        txtBmiValue.setText(String.format("%.1f", bmi));
        txtCategory.setText(category);
        resultCard.setVisibility(View.VISIBLE);

        // Show loading for AI advice
        txtAdvice.setText("");
        progressBar.setVisibility(View.VISIBLE);
        txtLoading.setVisibility(View.VISIBLE);
        btnCalculate.setEnabled(false);

        // Get personalised advice from Llama 3.1
        LlamaApiHelper.getBmiAdvice(bmi, category, h, w, new LlamaApiHelper.LlamaCallback() {
            @Override
            public void onSuccess(String response) {
                progressBar.setVisibility(View.GONE);
                txtLoading.setVisibility(View.GONE);
                btnCalculate.setEnabled(true);
                txtAdvice.setText("🤖 AI Advice:\n\n" + response);
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                txtLoading.setVisibility(View.GONE);
                btnCalculate.setEnabled(true);
                // Fallback to local advice if API fails
                txtAdvice.setText("🤖 AI Advice:\n\n" + getLocalAdvice(category) +
                        "\n\n(Generated offline — AI was unavailable)");
            }
        });
    }

    private String getCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25)   return "Normal weight";
        if (bmi < 30)   return "Overweight";
        return "Obese";
    }

    private String getLocalAdvice(String category) {
        switch (category) {
            case "Underweight":
                return "Your BMI suggests you may be underweight. Consider including more nutrient-rich foods and consulting a healthcare professional.\n\n⚠ This is general wellness information only — not medical advice.";
            case "Normal weight":
                return "Your BMI is in a healthy range. Keep maintaining a balanced diet and regular physical activity.\n\n⚠ This is general wellness information only — not medical advice.";
            case "Overweight":
                return "Your BMI is slightly above the healthy range. Small, consistent lifestyle changes can make a big difference.\n\n⚠ This is general wellness information only — not medical advice.";
            default:
                return "It is recommended to speak with a healthcare professional for a personalised health plan.\n\n⚠ This is general wellness information only — not medical advice.";
        }
    }
}
