package com.gbozza.android.popularmovies.data;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gbozza.android.popularmovies.data.FavouriteMoviesContract.FavouriteMovieEntry;

public class FavouriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favouriteMoviesDb.db";
    private static final int VERSION = 1;

    FavouriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "      + FavouriteMovieEntry.TABLE_NAME + " (" +
                FavouriteMovieEntry._ID                  + " INTEGER PRIMARY KEY, " +
                FavouriteMovieEntry.COLUMN_MOVIE_ID      + " INTEGER NOT NULL, " +
                FavouriteMovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_POSTER_PATH   + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_OVERVIEW      + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_TITLE         + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_RELEASE_DATE  + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_VOTE_AVERAGE  + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteMovieEntry.TABLE_NAME);
        onCreate(db);
    }

}
