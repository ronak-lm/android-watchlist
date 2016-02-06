package com.ronakmanglani.watchlist.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MovieProvider extends ContentProvider {

    private static final String AUTHORITY = "com.ronakmanglani.watchlist.movieprovider";
    private static final String BASE_PATH = "favorites";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int MOVIE = 1;
    private static final int MOVIE_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, MOVIE);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MOVIE_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        MovieOpenHelper helper = new MovieOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return database.query(MovieOpenHelper.TABLE_FAVORITES, MovieOpenHelper.ALL_COLUMNS, selection, null, null, null,
                MovieOpenHelper.MOVIE_TITLE + " ASC");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        long id = database.insert(MovieOpenHelper.TABLE_FAVORITES, null, contentValues);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return database.delete(MovieOpenHelper.TABLE_FAVORITES, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return database.update(MovieOpenHelper.TABLE_FAVORITES, contentValues, selection, selectionArgs);
    }
}
