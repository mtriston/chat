package edu.school21.sockets.models;

public class JSONMessage {
    public String message;
    public String author;
    public String room;

    public JSONMessage(String message, String author, String room) {
        this.message = message;
        this.author = author;
        this.room = room;
    }
}