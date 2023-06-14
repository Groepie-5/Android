package com.example.streamapplication.models;

public class Message {
    public String messageText;
    public String sender;

    public Message(String messageText, String sender) {
        this.messageText = messageText;
        this.sender = sender;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender;
    }
}
