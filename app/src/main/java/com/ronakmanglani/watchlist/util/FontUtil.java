package com.ronakmanglani.watchlist.util;

import android.content.Context;
import android.graphics.Typeface;

import com.ronakmanglani.watchlist.WatchlistApp;

import java.util.Hashtable;

public class FontUtil {

    public static final String ROBOTO_REGULAR = "fonts/Roboto-Regular.ttf";
    public static final String ROBOTO_LIGHT = "fonts/Roboto-Light.ttf";
    public static final String ROBOTO_BOLD = "fonts/Roboto-Bold.ttf";

    // Constructor
    private FontUtil() { }

    // Cache fonts in hash table
    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();
    public static Typeface getTypeface(String name) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(WatchlistApp.getAppContext().getAssets(), name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}
