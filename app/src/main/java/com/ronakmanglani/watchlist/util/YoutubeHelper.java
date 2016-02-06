package com.ronakmanglani.watchlist.util;

public class YoutubeHelper {

    // Get video URL
    public static String getVideoURL(String youtubeID) {
        return "https://www.youtube.com/watch?v=" + youtubeID;
    }

    // Get image URL of the video
    public static String getThumbnailURL(String youtubeID) {
        return "http://img.youtube.com/vi/" + youtubeID + "/0.jpg";
    }
}
