package com.example.bumin_chatbot;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_LOADING = 3;

    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.isLoading()) {
            return VIEW_TYPE_LOADING;
        } else if (message.isUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_BOT) {
            View view = inflater.inflate(R.layout.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        }
        // No binding needed for loading view
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView userMessageText;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            userMessageText = itemView.findViewById(R.id.userMessageText);
        }

        void bind(ChatMessage message) {
            userMessageText.setText(message.getMessage());
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView botMessageText;

        BotMessageViewHolder(View itemView) {
            super(itemView);
            botMessageText = itemView.findViewById(R.id.botMessageText);
        }

        void bind(ChatMessage message) {
            botMessageText.setText(message.getMessage());
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar loadingProgressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);
            loadingProgressBar = itemView.findViewById(R.id.loadingProgressBar);
        }
    }
}
