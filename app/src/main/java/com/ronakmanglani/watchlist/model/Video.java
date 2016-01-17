package com.ronakmanglani.watchlist.model;

public class Video {

    // Attributes
    public String title;
    public String youtubeID;
    public String imageURL;
    public String videoURL;

    // Constructor
    public Video(String title, String youtubeID, String imageURL, String videoURL) {
        this.title = title;
        this.youtubeID = youtubeID;
        this.imageURL = imageURL;
        this.videoURL = videoURL;
    }
}
