package com.ronakmanglani.watchlist.model;

import java.io.Serializable;

public class MovieThumb implements Serializable {

    public String id;
    public String name;
    public String year;
    public String rating;
    public String imageBaseURL;

    public MovieThumb(String id, String name, String year, String rating, String imageBaseURL) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.rating = rating;
        this.imageBaseURL = imageBaseURL;
    }
}
