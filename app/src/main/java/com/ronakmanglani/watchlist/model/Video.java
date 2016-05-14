package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable {

    // Attributes
    public String title;
    public String size;
    public String youtubeID;
    public String imageURL;
    public String videoURL;

    // Constructors
    public Video(String title, String size, String youtubeID, String imageURL, String videoURL) {
        this.title = title;
        this.size = size;
        this.youtubeID = youtubeID;
        this.imageURL = imageURL;
        this.videoURL = videoURL;
    }
    public Video(Parcel in) {
        this.title = in.readString();
        this.size = in.readString();
        this.youtubeID = in.readString();
        this.imageURL = in.readString();
        this.videoURL = in.readString();
    }

    // Parcelable Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    // Parcelling methods
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(title);
        out.writeString(size);
        out.writeString(youtubeID);
        out.writeString(imageURL);
        out.writeString(videoURL);
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
