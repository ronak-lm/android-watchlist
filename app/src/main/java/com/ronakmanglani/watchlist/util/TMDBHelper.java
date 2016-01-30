package com.ronakmanglani.watchlist.util;

import android.content.Context;

import com.ronakmanglani.watchlist.R;

public class TMDBHelper {

    // API key for TMDB
    private static String getApiKey(Context context) {
        return context.getString(R.string.api_key);
    }

    // API Endpoints
    public static String getMostPopularMoviesLink(Context context, int page) {
        return "http://api.themoviedb.org/3/movie/popular?&page=" + page + "&api_key=" + getApiKey(context);
    }
    public static String getHighestRatedMoviesLink(Context context, int page) {
        return "http://api.themoviedb.org/3/movie/top_rated?&page=" + page + "&api_key=" + getApiKey(context);
    }
    public static String getUpcomingMoviesLink(Context context, int page) {
        return "http://api.themoviedb.org/3/movie/upcoming?&page=" + page + "&api_key=" + getApiKey(context);
    }
    public static String getNowPlayingMoviesLink(Context context, int page) {
        return "http://api.themoviedb.org/3/movie/now_playing?&page=" + page + "&api_key=" + getApiKey(context);
    }
    public static String getMovieDetailLink(Context context, String id) {
        return "http://api.themoviedb.org/3/movie/" + id + "?api_key=" + getApiKey(context) + "&append_to_response=credits,trailers";
    }
    public static String getMovieReviewsLink(Context context, String id, int page) {
        return "http://api.themoviedb.org/3/movie/" + id + "/reviews?page=" + page + "&api_key=" + getApiKey(context);
    }
    public static String getWriteReviewLink(String id) {
        return "https://www.themoviedb.org/review/new?movie_id=" + id;
    }

    // Get image of the given pixel size
    public static String getImageURL(String baseURL, int widthPx) {
        if (widthPx > 500) {
            return "http://image.tmdb.org/t/p/w780/" + baseURL;
        } else if (widthPx > 342 && widthPx <= 500) {
            return "http://image.tmdb.org/t/p/w500/" + baseURL;
        } else if (widthPx > 185 && widthPx <= 342) {
            return "http://image.tmdb.org/t/p/w342/" + baseURL;
        } else if (widthPx > 154 && widthPx <= 185) {
            return "http://image.tmdb.org/t/p/w185/" + baseURL;
        } else if (widthPx > 92 && widthPx <= 154) {
            return "http://image.tmdb.org/t/p/w154/" + baseURL;
        } else if (widthPx > 0 && widthPx <= 92) {
            return "http://image.tmdb.org/t/p/w92/" + baseURL;
        } else {
            return "http://image.tmdb.org/t/p/w185/" + baseURL;     // Default Value
        }
    }

    // URL to share the movie
    public static String getMovieShareURL(String id) {
        return "https://www.themoviedb.org/movie/" + id;
    }
}
