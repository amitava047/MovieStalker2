package com.example.amitava.moviestalker.Data;

import android.provider.BaseColumns;

//A Contract class for SQLiteDatabase
public class FavoriteListContract {

    //An inner class containing information regarding Database
    public static final class FavoriteListEntry implements BaseColumns {
        //The Table name
        public static final String TABLE_NAME = "favorites";
        //The columns inside table
        public static final String COLUMN_MOVIE_TITLE = "title";
    }
}
