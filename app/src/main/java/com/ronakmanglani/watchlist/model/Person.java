package com.ronakmanglani.watchlist.model;

public class Person {

    // Attributes
    public String id;
    public String name;
    public String placeOfBirth;
    public String birthDay;
    public String deathDay;
    public String biography;
    public String homepage;
    public String imagePath;

    // Constructor
    public Person(String id, String name, String placeOfBirth, String birthDay, String deathDay,
                  String biography, String homepage, String imagePath) {
        this.id = id;
        this.name = name;
        this.placeOfBirth = placeOfBirth;
        this.birthDay = birthDay;
        this.deathDay = deathDay;
        this.biography = biography;
        this.homepage = homepage;
        this.imagePath = imagePath;
    }
}
