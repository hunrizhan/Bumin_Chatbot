package com.example.bumin_chatbot;



import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize views
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);

        // Set up RecyclerView
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Add welcome message
        messageList.add(new ChatMessage("Hello! I'm your AI assistant. How can I help you today?", false));
        chatAdapter.notifyItemInserted(0);

        // Set click listener for send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText) || isLoading) {
            return;
        }

        // Add user message to chat
        messageList.add(new ChatMessage(messageText, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        // Clear input field
        editTextMessage.setText("");

        // Scroll to bottom
        recyclerViewChat.smoothScrollToPosition(messageList.size() - 1);

        // Show loading indicator
        isLoading = true;
        messageList.add(new ChatMessage("", false, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        // Simulate AI response after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Remove loading indicator
                messageList.remove(messageList.size() - 1);
                chatAdapter.notifyItemRemoved(messageList.size());

                // Add AI response
                messageList.add(new ChatMessage(getRandomResponse(messageText), false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);

                // Scroll to bottom
                recyclerViewChat.smoothScrollToPosition(messageList.size() - 1);

                // Reset loading flag
                isLoading = false;
            }
        }, 1500); // 1.5 second delay
    }

    private String getRandomResponse(String userMessage) {
        String[] responses = {
                "I understand you're asking about \"" + userMessage + "\". That's an interesting topic!",
                "Thanks for your message about \"" + userMessage + "\". Let me help you with that.",
                "I've processed your query about \"" + userMessage + "\" and here's what I found...",
                "Regarding \"" + userMessage + "\", I have some information that might be helpful.",
                "I've analyzed your question about \"" + userMessage + "\" and here's my response."
        };

        return responses[new Random().nextInt(responses.length)];
    }
}
