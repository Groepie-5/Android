package com.example.streamapplication.models;

public class TransparantPerson extends TruYouAccount {
    public int satoshi;
    public String tagline;

    public TransparantPerson(int satoshi, String tagline) {
        super(1, "Jan");
        this.satoshi = satoshi;
        this.tagline = tagline;
    }
}
