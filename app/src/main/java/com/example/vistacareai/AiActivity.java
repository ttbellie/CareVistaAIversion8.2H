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
 * AiActivity — AI Health Q&A powered by Llama 3.1 via Groq API.
 *
 * LLM capability: Contextual Q&A within the health/wellness domain.
 * Integration mode: Hybrid (device UI + remote inference via Groq).
 *
 * Safety handling:
 *   - Emergency keywords trigger an immediate local response (no API call).
 *   - The LLM system prompt enforces no-diagnosis / no-prescription rules.
 *   - Every response includes a medical disclaimer.
 *
 * Privacy:
 *   - The user's question text is sent to Groq for inference.
 *   - No data is stored server-side.
 */
public class AiActivity extends AppCompatActivity {

    private EditText edtQuestion;
    private Button btnAsk;
    private LinearLayout responseCard;
    private TextView txtResponse;
    private ProgressBar progressBar;
    private TextView txtLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        edtQuestion  = findViewById(R.id.edtQuestion);
        btnAsk       = findViewById(R.id.btnAsk);
        responseCard = findViewById(R.id.responseCard);
        txtResponse  = findViewById(R.id.txtResponse);
        progressBar  = findViewById(R.id.progressBar);
        txtLoading   = findViewById(R.id.txtLoading);

        btnAsk.setOnClickListener(v -> handleQuestion());
    }

    private void handleQuestion() {
        String question = edtQuestion.getText().toString().trim();

        if (question.isEmpty()) {
            Toast.makeText(this, "Please enter a question.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Safety check: emergency keywords handled locally (no API call needed)
        if (isEmergency(question)) {
            showResponse("⚠ This sounds like an emergency.\n\n" +
                    "Please call 000 (Australia) or your local emergency number immediately.\n\n" +
                    "If you or someone else is in danger, do not delay seeking help.\n\n" +
                    "Lifeline Australia: 13 11 14\n" +
                    "Beyond Blue: 1300 22 4636");
            return;
        }

        // Show loading state
        setLoading(true);

        // Call Llama 3.1 via Groq API
        LlamaApiHelper.askQuestion(question, new LlamaApiHelper.LlamaCallback() {
            @Override
            public void onSuccess(String response) {
                setLoading(false);
                showResponse(response);
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                showResponse("⚠ Could not get a response.\n\n" + errorMessage +
                        "\n\nPlease check your internet connection and try again.");
            }
        });
    }

    private boolean isEmergency(String text) {
        String q = text.toLowerCase();
        return q.contains("chest pain") || q.contains("can't breathe") || q.contains("cannot breathe")
                || q.contains("suicide") || q.contains("suicidal") || q.contains("overdose")
                || q.contains("unconscious") || q.contains("heart attack")
                || q.contains("kill myself") || q.contains("want to die")
                || q.contains("severe bleeding") || q.contains("choking");
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        txtLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAsk.setEnabled(!loading);
        btnAsk.setText(loading ? "Thinking..." : "ASK AI");

        if (loading) {
            responseCard.setVisibility(View.GONE);
        }
    }

    private void showResponse(String text) {
        txtResponse.setText(text);
        responseCard.setVisibility(View.VISIBLE);
    }
}
