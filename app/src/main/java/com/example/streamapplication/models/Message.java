package com.example.streamapplication.models;

import java.util.Date;

public class Message {
    public String messageText;
    public TruYouAccount sender;

    public TransparantPerson receiver;
    public Date timestamp;

    public String signature;

    public Message(String messageText, TruYouAccount sender, TransparantPerson receiver, Date timestamp) {
        this.messageText = messageText;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender.name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getReceiver() {
        return receiver.name;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
