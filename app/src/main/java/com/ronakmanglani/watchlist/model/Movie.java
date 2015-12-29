package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    // Attributes
    public String id;
    public String name;
    public String year;
    public String rating;
    public String imageBaseURL;

    // Constructors
    public Movie(String id, String name, String year, String rating, String imageBaseURL) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.rating = rating;
        this.imageBaseURL = imageBaseURL;
    }

    // Parcelling methods
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    public Movie(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.year = in.readString();
        this.rating = in.readString();
        this.imageBaseURL = in.readString();
    }
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(year);
        out.writeString(rating);
        out.writeString(imageBaseURL);
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
