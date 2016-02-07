package com.ronakmanglani.watchlist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieOpenHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    // Table and columns
    public static final String TABLE_FAVORITES = "favorites";
    public static final String MOVIE_ID = "_id";
    public static final String MOVIE_TMDB_ID = "movieId";
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_TAGLINE = "tagline";
    public static final String MOVIE_DATE = "date";
    public static final String MOVIE_RUNTIME = "runtime";
    public static final String MOVIE_OVERVIEW = "overview";
    public static final String MOVIE_VOTE_AVG = "voteAverage";
    public static final String MOVIE_VOTE_COUNT = "voteCount";
    public static final String MOVIE_BACKDROP_IMAGE = "backdropImage";
    public static final String MOVIE_POSTER_IMAGE = "posterImage";
    public static final String MOVIE_VIDEO = "video";
    public static final String MOVIE_CAST = "cast";
    public static final String MOVIE_CREW = "crew";

    public static final String[] ALL_COLUMNS = {MOVIE_ID, MOVIE_TMDB_ID, MOVIE_TITLE, MOVIE_TAGLINE, MOVIE_DATE, MOVIE_RUNTIME,
                    MOVIE_OVERVIEW, MOVIE_VOTE_AVG, MOVIE_VOTE_COUNT, MOVIE_BACKDROP_IMAGE,
                    MOVIE_POSTER_IMAGE, MOVIE_VIDEO, MOVIE_CAST, MOVIE_CREW};

    // Create table SQL statement
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    MOVIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MOVIE_TMDB_ID + " TEXT, " +
                    MOVIE_TITLE + " TEXT, " +
                    MOVIE_TAGLINE + " TEXT, " +
                    MOVIE_DATE + " TEXT, " +
                    MOVIE_RUNTIME + " TEXT, " +
                    MOVIE_OVERVIEW + " TEXT, " +
                    MOVIE_VOTE_AVG + " TEXT, " +
                    MOVIE_VOTE_COUNT + " TEXT, " +
                    MOVIE_BACKDROP_IMAGE + " TEXT, " +
                    MOVIE_POSTER_IMAGE + " TEXT, " +
                    MOVIE_VIDEO + " TEXT, " +
                    MOVIE_CAST + " TEXT, " +
                    MOVIE_CREW + " TEXT" +
                    ")";

    public MovieOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
}
