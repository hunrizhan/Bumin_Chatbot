package com.example.bumin_chatbot;

public class Message {
    private String content;
    private boolean isUser;
    private boolean isLoading;

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
        this.isLoading = false;
    }

    public Message(String content, boolean isUser, boolean isLoading) {
        this.content = content;
        this.isUser = isUser;
        this.isLoading = isLoading;
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }

    public boolean isLoading() {
        return isLoading;
    }
}

