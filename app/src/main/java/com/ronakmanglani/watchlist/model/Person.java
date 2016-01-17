package com.ronakmanglani.watchlist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable {

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
    public Person(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.placeOfBirth = in.readString();
        this.birthDay = in.readString();
        this.deathDay = in.readString();
        this.biography = in.readString();
        this.homepage = in.readString();
        this.imagePath = in.readString();
    }

    // Parcelable Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    // Parcelling methods
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(placeOfBirth);
        out.writeString(birthDay);
        out.writeString(deathDay);
        out.writeString(biography);
        out.writeString(homepage);
        out.writeString(imagePath);
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
