package com.ronakmanglani.watchlist.model;

public class Video {

    // Youtube Video ID
    private String youtubeID;

    // Getters
    public String getVideoUrl() {
        return "https://www.youtube.com/watch?v=" + youtubeID;
    }
    public String getDetailUrl() {
        return "http://www.youtube.com/oembed?url=" + getVideoUrl() + "&format=json";
    }
    public String getThumbnailUrl() {
        return "http://img.youtube.com/vi/" + youtubeID + "/0.jpg";
    }

    // Constructor
    public Video(String youtubeID) {
        this.youtubeID = youtubeID;
    }
}
