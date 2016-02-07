package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.Serializable;

public class Credit implements Parcelable, Serializable {

    // Attributes
    public String id;
    public String name;
    public String role;
    public String imagePath;

    // Constructors
    public Credit(String id, String name, String role, String imagePath) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.imagePath = imagePath;
    }
    public Credit(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.role = in.readString();
        this.imagePath = in.readString();
    }

    // Parcelable Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Credit createFromParcel(Parcel in) {
            return new Credit(in);
        }
        public Credit[] newArray(int size) {
            return new Credit[size];
        }
    };

    // Parcelling methods
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(role);
        out.writeString(imagePath);
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
