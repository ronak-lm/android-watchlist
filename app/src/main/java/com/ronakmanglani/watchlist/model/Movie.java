package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    // Attributes
    public String id;
    public String title;
    public String year;
    public String overview;
    public String rating;
    public String posterImage;
    public String backdropImage;

    // Constructors
    public Movie(String id, String title, String year, String overview, String rating, String posterImage, String backdropImage) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.overview = overview;
        this.rating = rating;
        this.posterImage = posterImage;
        this.backdropImage = backdropImage;
    }
    public Movie(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.year = in.readString();
        this.overview = in.readString();
        this.rating = in.readString();
        this.posterImage = in.readString();
        this.backdropImage = in.readString();
    }

    // Parcelable Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    // Parcelling methods
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(title);
        out.writeString(year);
        out.writeString(overview);
        out.writeString(rating);
        out.writeString(posterImage);
        out.writeString(backdropImage);
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
