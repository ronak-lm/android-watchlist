package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {

    // Attributes
    public String id;
    public String author;
    public String body;
    public String url;

    // Constructors
    public Review(String id, String author, String body, String url) {
        this.id = id;
        this.author = author;
        this.body = body;
        this.url = url;
    }
    public Review(Parcel in) {
        this.id = in.readString();
        this.author = in.readString();
        this.body = in.readString();
        this.url = in.readString();
    }

    // Parcelable Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    // Parcelling methods
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(author);
        out.writeString(body);
        out.writeString(url);
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
