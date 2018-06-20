package ru.track.prefork;

import java.io.Serializable;

class Message implements Serializable {
    long ts;
    String data;
    String author;

    public Message(long ts, String data, String author) {
        this.ts = ts;
        this.data = data;
        this.author = author;
    }
}

