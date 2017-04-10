package com.gbozza.android.popularmovies.fragments;

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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.adapters.MoviesAdapter;
import com.gbozza.android.popularmovies.data.FavouriteMoviesContract.FavouriteMovieEntry;
import com.gbozza.android.popularmovies.models.Movie;
import com.gbozza.android.popularmovies.utilities.BottomRecyclerViewScrollListener;
import com.gbozza.android.popularmovies.utilities.MovieDbJsonUtilities;
import com.gbozza.android.popularmovies.utilities.NetworkUtilities;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A Class that extends Fragment to implement the Movie List structure
 */
public class MovieGridFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private Context mContext;
    private BottomRecyclerViewScrollListener mScrollListener;
    @BindView(R.id.rv_posters) RecyclerView mRecyclerView;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    @BindView(R.id.sr_swipe_container) SwipeRefreshLayout mSwipeContainer;
    @BindString(R.string.pref_sorting_key) String mPrefSortingKey;
    @BindString(R.string.pref_sorting_default) String mPrefSortingDefault;
    @BindString(R.string.pref_sorting_popular_value) String mPrefSortingPopularValue;
    @BindString(R.string.pref_sorting_rated_value) String mPrefSortingRatedValue;
    @BindString(R.string.pref_sorting_favourites_value) String mPrefSortingFavouritesValue;
    @BindString(R.string.pref_locale_key) String mPrefLocaleKey;
    @BindString(R.string.pref_locale_default) String mPrefLocaleDefault;
    private int mPage;
    private int mSorting;
    private static String mMovieLocale;
    private int mPosition = RecyclerView.NO_POSITION;

    private static MoviesAdapter mMoviesAdapter;

    private static final int SORTING_POPULAR = 1;
    private static final int SORTING_RATED = 2;
    private static final int SORTING_FAVOURITES = 3;
    private static final String BUNDLE_MOVIES_KEY = "movieList";
    private static final String BUNDLE_PAGE_KEY = "currentPage";
    private static final String BUNDLE_SORTING_KEY = "currentSorting";
    private static final String BUNDLE_ERROR_KEY = "errorShown";

    private static final int ID_FAVOURITES_LOADER = 33;

    public static final String[] FAVOURITE_MOVIES_PROJECTION = {
            FavouriteMovieEntry.COLUMN_MOVIE_ID,
            FavouriteMovieEntry.COLUMN_BACKDROP_PATH,
            FavouriteMovieEntry.COLUMN_POSTER_PATH,
            FavouriteMovieEntry.COLUMN_OVERVIEW,
            FavouriteMovieEntry.COLUMN_TITLE,
            FavouriteMovieEntry.COLUMN_RELEASE_DATE,
            FavouriteMovieEntry.COLUMN_VOTE_AVERAGE,
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Boolean errorShown = false;
        if (null != savedInstanceState) {
            errorShown = savedInstanceState.getBoolean(BUNDLE_ERROR_KEY);
        }

        View rootView = inflater.inflate(R.layout.movie_grid, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        setupSharedPreferences();

        if (null != savedInstanceState && !errorShown) {
            mPage = savedInstanceState.getInt(BUNDLE_PAGE_KEY);
            mSorting = savedInstanceState.getInt(BUNDLE_SORTING_KEY);
        } else {
            mPage = 1;
        }

        final int columns = getResources().getInteger(R.integer.grid_columns);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, columns, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mRecyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter();
        mRecyclerView.setAdapter(mMoviesAdapter);

        if (mSorting != SORTING_FAVOURITES) {
            mScrollListener = new BottomRecyclerViewScrollListener(gridLayoutManager, mPage) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    mPage = page;
                    loadCards();
                }
            };
            mRecyclerView.addOnScrollListener(mScrollListener);
        }

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mErrorMessageDisplay.setVisibility(View.INVISIBLE);
                clearGridView();
                loadCards();
            }
        });
        mSwipeContainer.setColorSchemeResources(R.color.colorAccent);

        if (null != savedInstanceState && !errorShown) {
            ArrayList<Movie> movieList = savedInstanceState.getParcelableArrayList(BUNDLE_MOVIES_KEY);
            mMoviesAdapter.setMoviesData(movieList);
        } else {
            loadCards();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<Movie> movieList = mMoviesAdapter.getMoviesData();
        if (null != movieList) {
            outState.putParcelableArrayList(BUNDLE_MOVIES_KEY, new ArrayList<>(movieList));
            outState.putInt(BUNDLE_PAGE_KEY, mPage);
            outState.putInt(BUNDLE_SORTING_KEY, mSorting);
        } else if (mErrorMessageDisplay.isShown()) {
            outState.putBoolean(BUNDLE_ERROR_KEY, true);
        }
    }

    /**
     * This method sets different options based on the SharedPreferences of the application
     */
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        String sorting = sharedPreferences.getString(mPrefSortingKey, mPrefSortingDefault);
        if (sorting.equals(mPrefSortingPopularValue)) {
            mSorting = SORTING_POPULAR;
        } else if (sorting.equals(mPrefSortingRatedValue)) {
            mSorting = SORTING_RATED;
        } else if (sorting.equals(mPrefSortingFavouritesValue)) {
            mSorting = SORTING_FAVOURITES;
        }

        mMovieLocale = sharedPreferences.getString(mPrefLocaleKey, mPrefLocaleDefault);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}

    /**
     * A method that invokes the AsyncTask to populate the RecyclerView,
     * it's based on the sorting option selected by the user. Default is "most popular"
     */
    public void loadCards() {
        if (mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(false);
        }
        if (NetworkUtilities.isOnline(mContext)) {
            switch (mSorting) {
                case SORTING_POPULAR:
                    new FetchMoviesTask().execute(
                            new String[]{
                                    NetworkUtilities.getMoviedbMethodPopular(),
                                    String.valueOf(mPage)
                            }
                    );
                    break;
                case SORTING_RATED:
                    new FetchMoviesTask().execute(
                            new String[]{
                                    NetworkUtilities.getMoviedbMethodRated(),
                                    String.valueOf(mPage)
                            }
                    );
                    break;
                case SORTING_FAVOURITES:
                    LoaderManager loaderManager = getActivity().getSupportLoaderManager();
                    if (null == loaderManager.getLoader(ID_FAVOURITES_LOADER)) {
                        loaderManager.initLoader(ID_FAVOURITES_LOADER, null, this);
                    } else {
                        loaderManager.restartLoader(ID_FAVOURITES_LOADER, null, this);
                    }
                    break;
            }
        } else {
            showErrorMessage(R.string.error_no_connectivity, mContext);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_FAVOURITES_LOADER:
                Uri favouriteMoviesUri = FavouriteMovieEntry.CONTENT_URI;
                return new CursorLoader(mContext,
                        favouriteMoviesUri,
                        FAVOURITE_MOVIES_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (null != data && data.getCount() != 0) {
            mMoviesAdapter.loadCursorIntoAdapter(data);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);
        } else {
            showErrorMessage(R.string.error_no_favourites, mContext);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.loadCursorIntoAdapter(null);
    }

    /**
     * Reset the GridView properties and adapter
     */
    public void clearGridView() {
        switch (mSorting) {
            case SORTING_POPULAR:
            case SORTING_RATED:
                mScrollListener.resetState();
                mPage = 1;
                mMoviesAdapter.clearMovieList();
                break;
            case SORTING_FAVOURITES:
                mMoviesAdapter.loadCursorIntoAdapter(null);
        }
    }

    /**
     * Display the specific error message in the TextView
     *
     * @param messageId the resource id of the error string
     */
    public void showErrorMessage(int messageId, Context context) {
        mErrorMessageDisplay.setText(context.getText(messageId));
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * Getter method for the Movie Locale
     *
     * @return a string representing the locale used to query The MovieDB service
     */
    public static String getMovieLocale() {
        return mMovieLocale;
    }

    /**
     * The background worker that executes the calls to the MovieDB service.
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    public class FetchMoviesTask extends AsyncTask<String[], Void, List<Movie>> {

        private final String TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String[]... params) {
            String method = params[0][0];
            String page = params[0][1];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getMoviedbLanguageQueryParam(), MovieGridFragment.getMovieLocale());
            mapping.put(NetworkUtilities.getMoviedbPageQueryParam(), String.valueOf(page));

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponseFromHttpUrl(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return MovieDbJsonUtilities.getPopularMoviesListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (null != movieList) {
                mMoviesAdapter.setMoviesData(movieList);
                mErrorMessageDisplay.setVisibility(View.INVISIBLE);
            } else {
                showErrorMessage(R.string.error_moviedb_list, mContext);
            }
            mSwipeContainer.setRefreshing(false);
        }
    }

}
