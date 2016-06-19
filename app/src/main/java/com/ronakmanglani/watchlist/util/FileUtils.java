package com.ronakmanglani.watchlist.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ronakmanglani.watchlist.database.MovieColumns;
import com.ronakmanglani.watchlist.database.MovieDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

    public static void copyFile(File source, File destination) throws IOException {
        FileInputStream fromFile = new FileInputStream(source);
        FileOutputStream toFile = new FileOutputStream(destination);
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    // Function to check if file is a valid database
    public static boolean isValidDbFile(File db) {
        try {
            // Open file as a database
            SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase(db.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            // Get cursors for both tables
            Cursor cursor1 = sqlDb.query(true, MovieDatabase.WATCHED, null, null, null, null, null, null, null);
            Cursor cursor2 = sqlDb.query(true, MovieDatabase.TO_SEE, null, null, null, null, null, null, null);
            // Check if "TMDB_ID" column exists (else throw exception)
            cursor1.getColumnIndexOrThrow(MovieColumns.TMDB_ID);
            cursor2.getColumnIndexOrThrow(MovieColumns.TMDB_ID);
            // Close database and cursors
            sqlDb.close();
            cursor1.close();
            cursor2.close();
            // No exceptions = Valid database
            return true;
        } catch (Exception e) {
            // Exception thrown - Invalid database
            return false;
        }
    }
}
