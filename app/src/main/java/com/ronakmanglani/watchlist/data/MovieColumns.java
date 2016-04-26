package com.ronakmanglani.watchlist.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

public interface MovieColumns {

    @DataType(INTEGER) @PrimaryKey @AutoIncrement String _ID = "_id";
    @DataType(TEXT) @NotNull String TMDB_ID = "tmdb_id";
    @DataType(TEXT) @NotNull String TITLE = "title";
    @DataType(TEXT) @NotNull String YEAR = "year";
    @DataType(TEXT) @NotNull String OVERVIEW = "overview";
    @DataType(TEXT) @NotNull String RATING = "rating";
    @DataType(TEXT) @NotNull String POSTER = "poster";
    @DataType(TEXT) @NotNull String BACKDROP = "backdrop";

}