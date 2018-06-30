package com.example.amitava.moviestalker.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.amitava.moviestalker.Data.FavoriteListContract.FavoriteListEntry;

public class FavoriteListDbHelper extends SQLiteOpenHelper {

    //The Database name
    public static final String DATABASE_NAME = "favoriteList.db";
    //The Database version
    public static final int DATABASE_VERSION = 2;

    //Default Constructor
    public FavoriteListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String createQuery = "CREATE TABLE " +
                FavoriteListEntry.TABLE_NAME + " (" +
                FavoriteListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteListEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteListEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
