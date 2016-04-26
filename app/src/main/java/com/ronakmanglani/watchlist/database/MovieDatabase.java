package com.ronakmanglani.watchlist.database;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = MovieDatabase.VERSION)
public class MovieDatabase {

    public static final int VERSION = 1;

    @Table(MovieColumns.class) public static final String WATCHED = "watched";
    @Table(MovieColumns.class) public static final String TO_SEE = "to_see";
}
