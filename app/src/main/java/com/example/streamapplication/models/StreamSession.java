package com.example.streamapplication.models;

public class StreamSession {
    public String name;
    public String streamTitle;


    public StreamSession(String name, String tagLine) {
        this.name = name;
        this.streamTitle = tagLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreamTitle() {
        return streamTitle;
    }

    public void setStreamTitle(String tagLine) {
        this.streamTitle = tagLine;
    }


}
