package com.ronakmanglani.watchlist.util;

public class TextUtil {

    // Constructor
    private TextUtil() { }

    // Check if given string is null or empty
    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.equals("null") || str.equals(""));
    }

}
