package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MovieDetail implements Parcelable {

    // Attributes
    public String id;
    public String title;
    public String tagline;
    public String releaseDate;
    public String runtime;
    public String overview;
    public String voteAverage;
    public String voteCount;
    public String backdropImage;
    public String posterImage;
    public ArrayList<String> genre;
    public ArrayList<String> videos;
    public ArrayList<Credit> cast;
    public ArrayList<Credit> crew;

    // Constructors
    public MovieDetail(String id, String title, String tagline, String releaseDate, String runtime, String overview,
                       String voteAverage, String voteCount, ArrayList<String> genre, String backdropImage,
                       String posterImage, ArrayList<String> videos, ArrayList<Credit> cast, ArrayList<Credit> crew) {
        this.id = id;
        this.title = title;
        this.tagline = tagline;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.genre = genre;
        this.backdropImage = backdropImage;
        this.posterImage = posterImage;
        this.videos = videos;
        this.cast = cast;
        this.crew = crew;
    }
    public MovieDetail(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.tagline = in.readString();
        this.releaseDate = in.readString();
        this.runtime = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readString();
        this.voteCount = in.readString();
        this.backdropImage = in.readString();
        this.posterImage = in.readString();
        in.readStringList(genre);
        in.readStringList(videos);
        in.readList(cast, Credit.class.getClassLoader());
        in.readList(crew, Credit.class.getClassLoader());
    }

    // Parcelable Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MovieDetail createFromParcel(Parcel in) {
            return new MovieDetail(in);
        }
        public MovieDetail[] newArray(int size) {
            return new MovieDetail[size];
        }
    };

    // Helper methods
    public String getSubtitle() {
        try {
            boolean isReleaseDateNull = (releaseDate == null || releaseDate.equals("null"));
            boolean isRuntimeNull = (runtime == null || runtime.equals("null") || runtime.equals("0"));

            if (isReleaseDateNull && isRuntimeNull) {
                return "";
            } else if (isReleaseDateNull) {
                return runtime + " mins";
            } else if (isRuntimeNull) {
                return getFormattedDate();
            } else {
                return getFormattedDate() + "\n" + runtime + " mins";
            }
        } catch (Exception ex) {
            return "";
        }
    }
    private String getFormattedDate() {
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = oldFormat.parse(releaseDate);
        } catch (Exception ex) { }
        SimpleDateFormat newFormat = new SimpleDateFormat("dd MMMM yyyy");
        return newFormat.format(date);
    }

    // Parcelling methods
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(title);
        out.writeString(tagline);
        out.writeString(releaseDate);
        out.writeString(runtime);
        out.writeString(overview);
        out.writeString(voteAverage);
        out.writeString(voteCount);
        out.writeString(backdropImage);
        out.writeString(posterImage);
        out.writeStringList(genre);
        out.writeStringList(videos);
        out.writeList(cast);
        out.writeList(crew);
    }
    @Override
    public int describeContents() {
        return 0;
    }
}