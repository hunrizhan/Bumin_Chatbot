package com.example.bumin_chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText messageInput;
    private ImageButton sendButton;
    private OkHttpClient httpClient;
    private boolean isLoading = false;
    private ExecutorService executorService;
    private Handler mainHandler;

    // API key is stored in BuildConfig
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize UI components
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Initialize chat messages list and adapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Initialize OkHttp client
        httpClient = new OkHttpClient();

        // Initialize executor service and handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Add welcome message
        chatMessages.add(new ChatMessage(getString(R.string.welcome_message), false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void logout() {
        // Clear any saved credentials
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Navigate back to login screen
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
            return true;
        }
        else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty() || isLoading) {
            return;
        }

        // Add user message to chat
        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);

        // Clear input field
        messageInput.setText("");

        // Scroll to bottom
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

        // Add loading message
        int loadingPosition = chatMessages.size();
        chatMessages.add(new ChatMessage("", false, true));
        chatAdapter.notifyItemInserted(loadingPosition);

        // Set loading flag
        isLoading = true;

        // Make API request
        makeGeminiApiRequest(message, loadingPosition);
    }

    private void makeGeminiApiRequest(String userMessage, final int loadingPosition) {
        String url = GEMINI_API_URL + "?key=" + API_KEY;

        try {
            // Create request body
            JSONObject requestBody = new JSONObject();
            JSONArray messages = new JSONArray();

            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", userMessage);
            parts.put(textPart);
            userMessageObj.put("parts", parts);

            messages.put(userMessageObj);
            requestBody.put("contents", messages);

            final String jsonBody = requestBody.toString();

            // Execute network request in background thread
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create request
                        RequestBody body = RequestBody.create(jsonBody, JSON);
                        Request request = new Request.Builder()
                                .url(url)
                                .post(body)
                                .header("Content-Type", "application/json")
                                .build();

                        // Execute request
                        Response response = httpClient.newCall(request).execute();

                        if (response.isSuccessful() && response.body() != null) {
                            String responseData = response.body().string();
                            System.out.println("Raw Gemini Response: " + responseData);
                            JSONObject jsonResponse = new JSONObject(responseData);

                            // Parse response
                            JSONArray candidates = jsonResponse.getJSONArray("candidates");
                            JSONObject candidate = candidates.getJSONObject(0);
                            // For new response structure: candidates[0].content.parts[0].text
                            JSONArray partsArray = candidate.getJSONObject("content").getJSONArray("parts");
                            final String botResponse = partsArray.getJSONObject(0).getString("text");

                            // Update UI on main thread
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // Remove loading message
                                    chatMessages.remove(loadingPosition);
                                    chatAdapter.notifyItemRemoved(loadingPosition);

                                    // Add bot response to chat
                                    chatMessages.add(new ChatMessage(botResponse, false));
                                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                                    // Scroll to bottom
                                    chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

                                    // Reset loading flag
                                    isLoading = false;
                                }
                            });
                        } else {
                            handleApiError(loadingPosition, "Error: " + response.code());
                        }
                    } catch (IOException | JSONException e) {
                        handleApiError(loadingPosition, "Error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            handleApiError(loadingPosition, "Error creating request: " + e.getMessage());
        }
    }

    private void handleApiError(final int loadingPosition, final String errorMessage) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                // Remove loading message
                chatMessages.remove(loadingPosition);
                chatAdapter.notifyItemRemoved(loadingPosition);

                // Add error message
                chatMessages.add(new ChatMessage("Sorry, I encountered an error: " + errorMessage, false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                // Scroll to bottom
                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

                // Show toast with error
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                // Reset loading flag
                isLoading = false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
