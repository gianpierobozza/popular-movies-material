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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.gbozza.android.popularmovies.data.FavouriteMoviesContract.FavouriteMovieEntry.TABLE_NAME;

public class FavouriteMoviesContentProvider extends ContentProvider {

    public static final int FAVOURITES = 100;
    public static final int FAVOURITES_WITH_MOVIEID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavouriteMoviesContract.AUTHORITY,
                FavouriteMoviesContract.PATH_FAVOURITES, FAVOURITES
        );
        uriMatcher.addURI(FavouriteMoviesContract.AUTHORITY,
                FavouriteMoviesContract.PATH_FAVOURITES + "/#", FAVOURITES_WITH_MOVIEID
        );

        return uriMatcher;
    }

    private FavouriteMoviesDbHelper mFavouriteMovieDbHelper;

    @Override
    public boolean onCreate() {

        Context context = getContext();
        mFavouriteMovieDbHelper = new FavouriteMoviesDbHelper(context);
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mFavouriteMovieDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVOURITES:
                long id = db.insert(TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(
                            FavouriteMoviesContract.FavouriteMovieEntry.CONTENT_URI, id
                    );
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mFavouriteMovieDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case FAVOURITES:
                retCursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVOURITES_WITH_MOVIEID:
                String movieId = uri.getPathSegments().get(1);
                retCursor =  db.query(TABLE_NAME,
                        projection,
                        "movieId=?",
                        new String[]{movieId},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mFavouriteMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int moviesDeleted;

        switch (match) {
            case FAVOURITES_WITH_MOVIEID:
                String movieId = uri.getPathSegments().get(1);
                moviesDeleted = db.delete(
                        TABLE_NAME,
                        FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{ movieId });
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (moviesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return moviesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
