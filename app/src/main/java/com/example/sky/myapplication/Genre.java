package com.example.sky.myapplication;

/**
 * Created by Devansh on 11/1/2015.
 */
public class Genre {
    String value;
    long genre_id;

    Genre(String a,long b){
        this.value = a;
        this.genre_id = b;
    }

    public String getGenreValue(){
        return this.value;
    }

    public long getGenre_id(){
        return this.genre_id;
    }
}
