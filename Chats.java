package com.example.instantchat;

public class Chats {

    private  long time;
    private  boolean seen;

    public Chats(long time, boolean seen) {
        this.time = time;
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Chats(){

    }
}
