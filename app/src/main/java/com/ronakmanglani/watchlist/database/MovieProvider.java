package com.ronakmanglani.watchlist.database;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = MovieProvider.AUTHORITY, database = MovieDatabase.class)
public class MovieProvider {

    public static final String AUTHORITY = "com.ronakmanglani.watchlist.database.MovieProvider";

    private MovieProvider() {
    }

    // Table for movies seen
    @TableEndpoint(table = MovieDatabase.WATCHED) public static class Watched {
        @ContentUri(
                path = "watched",
                type = "vnd.android.cursor.dir/list",
                defaultSort = MovieColumns.TITLE + " ASC")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/watched");

        private Watched() {
        }
    }

    // Table for movies to see
    @TableEndpoint(table = MovieDatabase.TO_SEE) public static class ToSee {
        @ContentUri(
                path = "to_see",
                type = "vnd.android.cursor.dir/list",
                defaultSort = MovieColumns.TITLE + " ASC")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/to_see");

        private ToSee() {
        }
    }
}
