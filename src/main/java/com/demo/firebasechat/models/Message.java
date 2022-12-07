package com.demo.firebasechat.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {

    private User user;
    private String message;
    private String message_id;
    private String message_trans;
    private @ServerTimestamp Date timestamp;

    public Message(User user, String message, String message_id, Date timestamp) {
        this.user = user;
        this.message = message;
        this.message_id = message_id;
        this.timestamp = timestamp;
    }

    public Message() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_trans() {
        return message_trans;
    }

    public void setMessage_trans(String message_trans) {
        this.message_trans = message_trans;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + user +
                ", message='" + message + '\'' +
                ", message_trans='" + message_trans + '\'' +
                ", message_id='" + message_id + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
