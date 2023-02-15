package it.unipi.dii.lsmd.socialrestaurant.model;

import java.util.Date;

public class Review {
    private String username;
    private String text;
    private Date timestamp;

    public Review(String username, String text, Date timestamp) {
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Review{" +
                "username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}