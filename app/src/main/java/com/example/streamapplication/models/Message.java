package com.example.streamapplication.models;

import java.util.Date;

public class Message {
    public String messageText;
    public String sender;
    public Date timestamp;

    public Message(String messageText, String sender, Date timestamp) {
        this.messageText = messageText;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
