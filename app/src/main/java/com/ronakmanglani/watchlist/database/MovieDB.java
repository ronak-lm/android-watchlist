package com.ronakmanglani.watchlist.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.ronakmanglani.watchlist.model.Movie;

import java.util.ArrayList;

public class MovieDB {

    // Constants
    private static final String MOVIE_DATABASE = "movie_database";
    private static final String FAVORITE_MOVIES = "favorite_movies";

    // Singleton
    private static MovieDB instance;
    public static MovieDB getInstance(Context context) {
        if (instance == null) {
            instance = new MovieDB(context);
        }
        return instance;
    }

    // Member objects
    private Context context;
    public ArrayList<Movie> movieList;

    // Constructor
    private MovieDB(Context context) {
        this.context = context;
        SharedPreferences preferences = context.getSharedPreferences(MOVIE_DATABASE, Context.MODE_PRIVATE);
        String json = preferences.getString(FAVORITE_MOVIES, "");
        if (json.length() == 0) {
            movieList = new ArrayList<>();
        } else {
            MovieWrapper wrapper = new Gson().fromJson(json, MovieWrapper.class);
            movieList = wrapper.getMovieList();
        }
    }

    // Helper methods
    public boolean containsMovie(String movieId) {
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).id.equals(movieId)) {
                return true;
            }
        }
        return false;
    }
    public void removeMovie(String movieId) {
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).id.equals(movieId)) {
                movieList.remove(i);
                break;
            }
        }
    }
    public void commit() {
        SharedPreferences preferences = context.getSharedPreferences(MOVIE_DATABASE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        MovieWrapper wrapper = new MovieWrapper(movieList);
        String json = new Gson().toJson(wrapper);
        editor.putString(FAVORITE_MOVIES, json);
        editor.apply();
    }
}
