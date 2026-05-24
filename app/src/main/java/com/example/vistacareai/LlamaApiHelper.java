package com.example.vistacareai;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * LlamaApiHelper — centralised helper for calling Llama 3.1 via the Groq API.
 *
 * Integration mode: Hybrid (device UI + remote inference).
 * Model: Llama 3.1 8B (meta-llama/llama-3.1-8b-instant) hosted on Groq.
 *
 * Privacy note:
 *   - The user's prompt text is sent to Groq's API servers for inference.
 *   - No personal health data is stored server-side; Groq does not train on API inputs.
 *   - All persistent data (period dates, health logs) stays on-device in SharedPreferences.
 */
public class LlamaApiHelper {

    // ── Configuration ──────────────────────────────────────────────────────────
    // Replace with your own Groq API key from https://console.groq.com/keys
    private static final String API_KEY = BuildConfig.GROQ_API_KEY;    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── System prompt ──────────────────────────────────────────────────────────
    private static final String SYSTEM_PROMPT =
            "You are VistaCare AI, a helpful women's health and wellness assistant. " +
            "You provide general wellness information about topics like sleep, hydration, " +
            "exercise, menstrual health, BMI, nutrition, stress management, and mental wellbeing. " +
            "IMPORTANT RULES:\n" +
            "1. Always include a disclaimer: 'This is general wellness information only — not medical advice.'\n" +
            "2. Never diagnose conditions or prescribe medication.\n" +
            "3. For emergencies (chest pain, breathing difficulty, suicidal thoughts, overdose), " +
            "   immediately tell the user to call 000 (Australia) or their local emergency number.\n" +
            "4. Keep responses concise (under 150 words) and friendly.\n" +
            "5. If a question is outside health/wellness, politely redirect to health topics.\n" +
            "6. Never generate harmful, sexual, or inappropriate content.";

    // ── Callback interface ─────────────────────────────────────────────────────
    public interface LlamaCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    // ── Public methods ─────────────────────────────────────────────────────────

    /**
     * Send a general health question to Llama 3.1.
     */
    public static void askQuestion(String userMessage, LlamaCallback callback) {
        callApi(SYSTEM_PROMPT, userMessage, callback);
    }

    /**
     * Get AI-powered BMI advice based on calculated values.
     */
    public static void getBmiAdvice(double bmi, String category, double height, double weight,
                                    LlamaCallback callback) {
        String prompt = String.format(
                "The user calculated their BMI. Height: %.2f m, Weight: %.1f kg, " +
                "BMI: %.1f, Category: %s. " +
                "Provide brief, personalised wellness advice based on this BMI result. " +
                "Be encouraging and suggest actionable steps. Keep it under 120 words.",
                height, weight, bmi, category);
        callApi(SYSTEM_PROMPT, prompt, callback);
    }

    /**
     * Get AI-powered health insights based on daily log data.
     */
    public static void getHealthInsight(int waterGlasses, double sleepHours, String mood,
                                        LlamaCallback callback) {
        String prompt = String.format(
                "The user logged their daily health data: " +
                "Water intake: %d glasses, Sleep: %.1f hours, Mood: %s. " +
                "Provide a brief, personalised wellness summary with 2-3 actionable tips " +
                "based on this data. Be warm and encouraging. Keep it under 120 words.",
                waterGlasses, sleepHours, mood);
        callApi(SYSTEM_PROMPT, prompt, callback);
    }

    /**
     * Get AI-powered period prediction insight.
     */
    public static void getPeriodInsight(String selectedDates, String historyDates,
                                        String predictedDate, LlamaCallback callback) {
        String prompt = String.format(
                "The user tracks their menstrual cycle. " +
                "Current selected period dates: %s. " +
                "Cycle history: %s. " +
                "Predicted next period start: %s. " +
                "Provide a brief, supportive insight about their cycle regularity and " +
                "any general wellness tips related to menstrual health. " +
                "Keep it under 120 words.",
                selectedDates, historyDates, predictedDate);
        callApi(SYSTEM_PROMPT, prompt, callback);
    }

    // ── Internal API call ──────────────────────────────────────────────────────

    private static void callApi(String systemPrompt, String userMessage, LlamaCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", MODEL);
                requestBody.put("messages", new JSONArray()
                        .put(new JSONObject().put("role", "system").put("content", systemPrompt))
                        .put(new JSONObject().put("role", "user").put("content", userMessage))
                );
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 512);

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), JSON_TYPE))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        postError(callback, "API error (" + response.code() + "): " + parseErrorMessage(errorBody));
                        return;
                    }

                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    String content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim();

                    postSuccess(callback, content);
                }

            } catch (IOException e) {
                postError(callback, "Network error: Please check your internet connection and try again.");
            } catch (Exception e) {
                postError(callback, "Something went wrong: " + e.getMessage());
            }
        });
    }

    private static String parseErrorMessage(String errorBody) {
        try {
            JSONObject json = new JSONObject(errorBody);
            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                return error.optString("message", "Unknown API error");
            }
        } catch (Exception ignored) {}
        return errorBody.length() > 200 ? errorBody.substring(0, 200) : errorBody;
    }

    private static void postSuccess(LlamaCallback callback, String result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private static void postError(LlamaCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
}
