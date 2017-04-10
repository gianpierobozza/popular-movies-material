package com.gbozza.android.popularmovies.utilities;

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

import android.util.Log;

import com.gbozza.android.popularmovies.models.Movie;
import com.gbozza.android.popularmovies.models.Review;
import com.gbozza.android.popularmovies.models.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to manage the JSON Object returned from the MovieDB API
 */
public final class MovieDbJsonUtilities {

    private static final String TMDB_STATUS_CODE = "status_code";
    private static final String TMDB_STATUS_MESSAGE = "status_message";
    private static final int TMDB_STATUS_INVALID_API_KEY = 7;
    private static final int TMDB_STATUS_INVALID_RESOUCE = 34;
    private static final String TMDB_SUCCESS = "success";

    private static final String TAG = MovieDbJsonUtilities.class.getSimpleName();

    /**
     * The method that takes a JSONObject and converts it to a List of Movies
     *
     * @param popularMovies the JSONObject from the service
     * @return a List<Movie> Object, a list containing Movie Objects
     * @throws JSONException
     */
    public static List<Movie> getPopularMoviesListFromJson(JSONObject popularMovies)
            throws JSONException {

        // root keys
        final String TMDB_RESULTS = "results";

        // "movies results" keys
        final String TMDB_R_M_BACKDROP_PATH = "backdrop_path";
        final String TMDB_R_M_POSTER_PATH = "poster_path";
        final String TMDB_R_M_ID = "id";
        final String TMDB_R_M_OVERVIEW = "overview";
        final String TMDB_R_M_ORIGINAL_TITLE = "original_title";
        final String TMDB_R_M_RELEASE_DATE = "release_date";
        final String TMDB_R_M_VOTE_AVERAGE = "vote_average";

        List<Movie> parsedMoviesData;

        // Error codes management
        if (popularMovies.has(TMDB_SUCCESS) && !popularMovies.getBoolean(TMDB_SUCCESS)) {
            int errorCode = popularMovies.getInt(TMDB_STATUS_CODE);
            String message = popularMovies.getString(TMDB_STATUS_MESSAGE);

            switch (errorCode) {
                case TMDB_STATUS_INVALID_API_KEY:
                    // Invalid API key provided
                    Log.d(TAG, message);
                    return null;
                case TMDB_STATUS_INVALID_RESOUCE:
                    // Invalid resource
                    Log.d(TAG, message);
                    return null;
                default:
                    // Server probably down
                    return null;
            }
        }

        JSONArray resultsArray = popularMovies.getJSONArray(TMDB_RESULTS);

        parsedMoviesData = new ArrayList<>();

        for (int i = 0; i < resultsArray.length(); i++) {
            int id;
            String backdropPath;
            String posterPath;
            String overview;
            String originalTitle;
            String popularity;
            String voteAverage;

            // Get the JSON object representing one movie result
            JSONObject result = resultsArray.getJSONObject(i);

            id = result.getInt(TMDB_R_M_ID);
            backdropPath = result.getString(TMDB_R_M_BACKDROP_PATH);
            posterPath = result.getString(TMDB_R_M_POSTER_PATH);
            overview = result.getString(TMDB_R_M_OVERVIEW);
            originalTitle = result.getString(TMDB_R_M_ORIGINAL_TITLE);
            popularity = result.getString(TMDB_R_M_RELEASE_DATE);
            voteAverage = result.getString(TMDB_R_M_VOTE_AVERAGE);

            Movie movie = new Movie(id, backdropPath, posterPath, overview, originalTitle, popularity, voteAverage);
            parsedMoviesData.add(movie);
        }

        return parsedMoviesData;
    }

    /**
     * The method that takes a JSONObject and converts it to a List of Videos
     *
     * @param videos the JSONObject from the service
     * @return a List<Video> Object, a list containing Video Objects
     * @throws JSONException
     */
    public static List<Video> getVideosListFromJson(JSONObject videos)
            throws JSONException {

        // root keys
        final String TMDB_RESULTS = "results";

        // "videos results" keys
        final String TMDB_R_V_ID = "id";
        final String TMDB_R_V_ISO_639_1 = "iso_639_1";
        final String TMDB_R_V_ISO_3166_1 = "iso_3166_1";
        final String TMDB_R_V_KEY = "key";
        final String TMDB_R_V_NAME = "name";
        final String TMDB_R_V_SITE = "site";
        final String TMDB_R_V_SIZE = "size";
        final String TMDB_R_V_TYPE = "type";

        List<Video> parsedVideosData;

        // Error codes management
        if (videos.has(TMDB_SUCCESS) && !videos.getBoolean(TMDB_SUCCESS)) {
            int errorCode = videos.getInt(TMDB_STATUS_CODE);
            String message = videos.getString(TMDB_STATUS_MESSAGE);

            switch (errorCode) {
                case TMDB_STATUS_INVALID_API_KEY:
                    // Invalid API key provided
                    Log.d(TAG, message);
                    return null;
                case TMDB_STATUS_INVALID_RESOUCE:
                    // Invalid resource
                    Log.d(TAG, message);
                    return null;
                default:
                    // Server probably down
                    return null;
            }
        }

        JSONArray resultsArray = videos.getJSONArray(TMDB_RESULTS);

        parsedVideosData = new ArrayList<>();

        for (int i = 0; i < resultsArray.length(); i++) {
            String id;
            String iso_639_1;
            String iso_3166_1;
            String key;
            String name;
            String site;
            String size;
            String type;

            // Get the JSON object representing one movie result
            JSONObject result = resultsArray.getJSONObject(i);

            id = result.getString(TMDB_R_V_ID);
            iso_639_1 = result.getString(TMDB_R_V_ISO_639_1);
            iso_3166_1 = result.getString(TMDB_R_V_ISO_3166_1);
            key = result.getString(TMDB_R_V_KEY);
            name = result.getString(TMDB_R_V_NAME);
            site = result.getString(TMDB_R_V_SITE);
            size = result.getString(TMDB_R_V_SIZE);
            type = result.getString(TMDB_R_V_TYPE);

            Video video = new Video(id, iso_639_1, iso_3166_1, key, name, site, size, type);
            parsedVideosData.add(video);
        }

        return parsedVideosData;
    }

    /**
     * The method that takes a JSONObject and converts it to a List of Reviews
     *
     * @param reviews the JSONObject from the service
     * @return a List<Review> Object, a list containing Review Objects
     * @throws JSONException
     */
    public static List<Review> getReviewsListFromJson(JSONObject reviews)
            throws JSONException {

        // root keys
        final String TMDB_RESULTS = "results";

        // "reviews results" keys
        final String TMDB_R_R_ID = "id";
        final String TMDB_R_R_AUTHOR = "author";
        final String TMDB_R_R_CONTENT = "content";
        final String TMDB_R_R_URL = "url";

        List<Review> parsedReviewsData;

        // Error codes management
        if (reviews.has(TMDB_SUCCESS) && !reviews.getBoolean(TMDB_SUCCESS)) {
            int errorCode = reviews.getInt(TMDB_STATUS_CODE);
            String message = reviews.getString(TMDB_STATUS_MESSAGE);

            switch (errorCode) {
                case TMDB_STATUS_INVALID_API_KEY:
                    // Invalid API key provided
                    Log.d(TAG, message);
                    return null;
                case TMDB_STATUS_INVALID_RESOUCE:
                    // Invalid resource
                    Log.d(TAG, message);
                    return null;
                default:
                    // Server probably down
                    return null;
            }
        }

        JSONArray resultsArray = reviews.getJSONArray(TMDB_RESULTS);

        parsedReviewsData = new ArrayList<>();

        for (int i = 0; i < resultsArray.length(); i++) {
            String id;
            String author;
            String content;
            String url;

            // Get the JSON object representing one movie result
            JSONObject result = resultsArray.getJSONObject(i);

            id = result.getString(TMDB_R_R_ID);
            author = result.getString(TMDB_R_R_AUTHOR);
            content = result.getString(TMDB_R_R_CONTENT);
            url = result.getString(TMDB_R_R_URL);

            Review review = new Review(id, author, content, url);
            parsedReviewsData.add(review);
        }

        return parsedReviewsData;
    }

}
