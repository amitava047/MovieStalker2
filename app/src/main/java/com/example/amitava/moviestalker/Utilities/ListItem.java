package com.example.amitava.moviestalker.Utilities;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.StringReader;

public class ListItem implements Parcelable {

    private String comment;
    private String username;

    //Constructor
    public ListItem(String username, String review){
        this.username = username;
        this.comment = review;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ListItem(Parcel parcel){
        this.comment = parcel.readString();
        this.username = parcel.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        @Override
        public ListItem createFromParcel(Parcel parcel) {
            return new ListItem(parcel);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.comment);
        parcel.writeString(this.username);
    }
}
