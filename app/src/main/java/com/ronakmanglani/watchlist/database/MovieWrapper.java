package com.ronakmanglani.watchlist.database;

import com.ronakmanglani.watchlist.model.Movie;

import java.io.Serializable;
import java.util.ArrayList;

public class MovieWrapper implements Serializable {

    private ArrayList<Movie> movieList;

    public MovieWrapper(ArrayList<Movie> data) {
        this.movieList = data;
    }

    public ArrayList<Movie> getMovieList() {
        return this.movieList;
    }
}
