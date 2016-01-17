package com.ronakmanglani.watchlist.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MovieDetail {

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
    public ArrayList<String> images;
    public ArrayList<String> videos;
    public ArrayList<Credit> cast;
    public ArrayList<Credit> crew;

    // Constructors
    public MovieDetail(String id, String title, String tagline, String releaseDate, String runtime,
                       String overview, String voteAverage, String voteCount, ArrayList<String> genre,
                       String backdropImage, String posterImage, ArrayList<String> images,
                       ArrayList<String> videos, ArrayList<Credit> cast, ArrayList<Credit> crew) {
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
        this.images = images;
        this.videos = videos;
        this.cast = cast;
        this.crew = crew;
    }

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
}