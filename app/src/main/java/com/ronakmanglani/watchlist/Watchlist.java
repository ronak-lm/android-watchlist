package com.ronakmanglani.watchlist;

import android.app.Application;

public class Watchlist extends Application {
    public static final String TABLE_USER = "user_settings";
    public static final String KEY_LAST_SELECTED = "drawer_selection";
    public static final String KEY_THUMBNAIL_SIZE = "thumbnail_size";
    public static final String KEY_VIEW_MODE = "view_mode";
    public static final int VIEW_MODE_GRID = 1;
    public static final int VIEW_MODE_LIST = 2;
}
