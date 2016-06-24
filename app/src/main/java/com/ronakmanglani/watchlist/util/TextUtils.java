package com.ronakmanglani.watchlist.util;

public class TextUtils {

    private TextUtils() {
    }

    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.equals("null") || str.equals(""));
    }

}
