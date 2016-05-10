package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {

    // Attributes
    public String id;
    public String userName;
    public String comment;
    public String createdAt;
    public boolean hasSpoiler;

    // Constructors
    public Review(String id, String userName, String comment, String createdAt, boolean hasSpoiler) {
        this.id = id;
        this.userName = userName;
        this.comment = comment;
        this.createdAt = createdAt;
        this.hasSpoiler = hasSpoiler;
    }
    public Review(Parcel in) {
        this.id = in.readString();
        this.userName = in.readString();
        this.comment = in.readString();
        this.createdAt = in.readString();
        this.hasSpoiler = (in.readInt() == 1);
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
        out.writeString(userName);
        out.writeString(comment);
        out.writeString(createdAt);
        if (hasSpoiler) {
            out.writeInt(1);
        } else {
            out.writeInt(0);
        }
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
