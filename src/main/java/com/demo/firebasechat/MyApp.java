package com.demo.firebasechat;

import android.app.Application;

import com.demo.firebasechat.models.User;

public class MyApp extends Application {

    private User user = null;
    private String lang = "en";

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
