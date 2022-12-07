package com.demo.firebasechat.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Room implements Parcelable {

    private String title;
    private String chatroom_id;


    public Room(String title, String chatroom_id) {
        this.title = title;
        this.chatroom_id = chatroom_id;
    }

    public Room() {

    }

    protected Room(Parcel in) {
        title = in.readString();
        chatroom_id = in.readString();
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "title='" + title + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(chatroom_id);
    }
}
