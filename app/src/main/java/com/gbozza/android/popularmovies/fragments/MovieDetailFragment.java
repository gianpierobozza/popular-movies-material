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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.adapters.ReviewsAdapter;
import com.gbozza.android.popularmovies.adapters.VideosAdapter;
import com.gbozza.android.popularmovies.data.FavouriteMoviesContract;
import com.gbozza.android.popularmovies.models.Movie;
import com.gbozza.android.popularmovies.models.Review;
import com.gbozza.android.popularmovies.models.Video;
import com.gbozza.android.popularmovies.utilities.MovieDbJsonUtilities;
import com.gbozza.android.popularmovies.utilities.NetworkUtilities;
import com.gbozza.android.popularmovies.utilities.SpannableUtilities;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailFragment extends Fragment {

    private Context mContext;
    private Movie mMovie;
    private VideosAdapter mVideosAdapter;
    private ReviewsAdapter mReviewsAdapter;

    @BindView(R.id.collapsing_toolbar_movie_detail)
    CollapsingToolbarLayout mMovieCollapsingToolbarLayout;
    @BindView(R.id.toolbar_movie_detail)
    Toolbar mMovieToolbar;
    @BindView(R.id.backdrop_movie_detail_scrolling_top)
    ImageView mMovieBackdropImageView;
    @BindView(R.id.tv_movie_detail_vote_average)
    TextView mMovieVoteAverageTextView;
    @BindView(R.id.tv_movie_detail_release_date)
    TextView mMovieReleaseDateTextView;
    @BindView(R.id.tv_movie_detail_overview)
    TextView mMovieOverviewTextView;

    @BindView(R.id.rv_videos)
    RecyclerView mVideosRecyclerView;
    @BindView(R.id.rv_reviews)
    RecyclerView mReviewsRecyclerView;

    @BindString(R.string.movie_detail_vote_average)
    String mDetailVoteAvgLabel;
    @BindString(R.string.movie_detail_release_date)
    String mDetailReleaseDateLabel;
    @BindString(R.string.movie_detail_overview)
    String mDetailOverviewLabel;

    @BindString(R.string.movie_favourite_off_toast_msg)
    String mFavOffToastMsg;
    @BindString(R.string.movie_favourite_on_toast_msg)
    String mFavOnToastMsg;

    public static final String PARCELABLE_MOVIE_KEY = "movieObject";
    private static final String BUNDLE_VIDEOS_KEY = "videoList";
    private static final String BUNDLE_REVIEWS_KEY = "reviewList";

    private static final String DETAIL_ELEMENT_VIDEOS = "videos";
    private static final String DETAIL_ELEMENT_REVIEWS = "reviews";

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        mMovie = null;
        if (getArguments().containsKey(PARCELABLE_MOVIE_KEY)) {
            mMovie = getArguments().getParcelable(PARCELABLE_MOVIE_KEY);
        }

        if (null != mMovie) {
            View rootView = inflater.inflate(R.layout.movie_detail, container, false);
            ButterKnife.bind(this, rootView);
            mMovieCollapsingToolbarLayout.setTitle(mMovie.getOriginalTitle());

            ((AppCompatActivity) getActivity()).setSupportActionBar(mMovieToolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                setHasOptionsMenu(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            Picasso.with(mContext)
                    .load(mMovie.buildBackdropPath(mContext))
                    .into(mMovieBackdropImageView);

            mMovieVoteAverageTextView.append(SpannableUtilities.makeBold(mDetailVoteAvgLabel));
            mMovieVoteAverageTextView.append(mMovie.getVoteAverage());
            mMovieReleaseDateTextView.append(SpannableUtilities.makeBold(mDetailReleaseDateLabel));
            mMovieReleaseDateTextView.append(mMovie.getReleaseDate());
            mMovieOverviewTextView.append(SpannableUtilities.makeBold(mDetailOverviewLabel));
            mMovieOverviewTextView.append(mMovie.getOverview());

            LinearLayoutManager videosLinearLayoutManager = new LinearLayoutManager(mContext);
            videosLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mVideosRecyclerView.setLayoutManager(videosLinearLayoutManager);

            mVideosRecyclerView.setHasFixedSize(true);
            mVideosAdapter = new VideosAdapter();
            mVideosRecyclerView.setAdapter(mVideosAdapter);

            LinearLayoutManager reviewsLinearLayoutManager = new LinearLayoutManager(mContext);
            mReviewsRecyclerView.setLayoutManager(reviewsLinearLayoutManager);

            mReviewsRecyclerView.setHasFixedSize(true);
            mReviewsAdapter = new ReviewsAdapter();
            mReviewsRecyclerView.setAdapter(mReviewsAdapter);

            if (null != savedInstanceState) {
                ArrayList<Video> videoList = savedInstanceState.getParcelableArrayList(BUNDLE_VIDEOS_KEY);
                mVideosAdapter.setVideosData(videoList);
                ArrayList<Review> reviewList = savedInstanceState.getParcelableArrayList(BUNDLE_REVIEWS_KEY);
                mReviewsAdapter.setReviewsData(reviewList);
            } else {
                loadElements(DETAIL_ELEMENT_VIDEOS, mMovie.getId());
                loadElements(DETAIL_ELEMENT_REVIEWS, mMovie.getId());
            }
            return rootView;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<Video> videosList = mVideosAdapter.getVideosData();
        if (null != videosList) {
            ArrayList<Video> videoArrayList = new ArrayList<>(videosList);
            outState.putParcelableArrayList(BUNDLE_VIDEOS_KEY, videoArrayList);
        }

        List<Review> reviewsList = mReviewsAdapter.getReviewsData();
        if (null != reviewsList) {
            ArrayList<Review> reviewArrayList = new ArrayList<>(reviewsList);
            outState.putParcelableArrayList(BUNDLE_REVIEWS_KEY, reviewArrayList);
        }
    }

    /**
     * A method that invokes the AsyncTask to populate the details required, for example
     * video trailer or reviews.
     *
     * @param element the element type to load
     * @param movieId the movie id for the specific videos we need
     */
    public void loadElements(String element, int movieId) {
        if (NetworkUtilities.isOnline(mContext)) {
            String method;
            switch (element) {
                case DETAIL_ELEMENT_VIDEOS:
                    method = NetworkUtilities.getMoviedbMethodVideos(movieId);
                    String[] videos = new String[]{method};
                    new FetchVideosTask().execute(videos);
                    break;
                case DETAIL_ELEMENT_REVIEWS:
                    method = NetworkUtilities.getMoviedbMethodReviews(movieId);
                    String[] reviews = new String[]{method};
                    new FetchReviewsTask().execute(reviews);
                    break;
            }
        }
    }

    /**
     * A method to check if a Movie is already or not flagged as favourite
     *
     * @param movieId the ID of the movie, from The MovieDB database
     * @return true or false
     */
    private boolean checkFavourite(int movieId) {
        boolean favourite = false;
        String[] selectionArgs = {String.valueOf(movieId)};
        Uri uri = FavouriteMoviesContract.FavouriteMovieEntry.buildFavouriteUriWithMovieId(movieId);
        Cursor cursor = getActivity().getContentResolver().query(uri,
                null,
                FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_MOVIE_ID + "=?",
                selectionArgs,
                null);
        if (null != cursor && cursor.getCount() != 0) {
            favourite = true;
            cursor.close();
        }
        return favourite;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
        if (checkFavourite(mMovie.getId())) {
            menu.getItem(0).setIcon(R.drawable.ic_star);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.action_favourite:
                if (checkFavourite(mMovie.getId())) {
                    Uri removeFavouriteUri = FavouriteMoviesContract.FavouriteMovieEntry.buildFavouriteUriWithMovieId(mMovie.getId());
                    getActivity().getContentResolver().delete(removeFavouriteUri, null, null);
                    Toast.makeText(getActivity().getBaseContext(), mFavOffToastMsg, Toast.LENGTH_LONG).show();
                    item.setIcon(R.drawable.ic_star_border_white);
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
                    contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_BACKDROP_PATH, mMovie.getBackdropPath());
                    contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
                    contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
                    contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_TITLE, mMovie.getOriginalTitle());
                    contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
                    contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());
                    Uri favouriteUri = getActivity().getContentResolver().insert(FavouriteMoviesContract.FavouriteMovieEntry.CONTENT_URI, contentValues);

                    if (null != favouriteUri) {
                        Toast.makeText(getActivity().getBaseContext(), mFavOnToastMsg, Toast.LENGTH_LONG).show();
                        item.setIcon(R.drawable.ic_star);
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The background worker that executes the calls to the MovieDB service
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    private class FetchVideosTask extends AsyncTask<String[], Void, List<Video>> {

        private final String TAG = FetchVideosTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Video> doInBackground(String[]... params) {
            String method = params[0][0];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getMoviedbLanguageQueryParam(), MovieGridFragment.getMovieLocale());

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponseFromHttpUrl(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return MovieDbJsonUtilities.getVideosListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Video> videoList) {
            if (!(videoList.isEmpty())) {
                mVideosAdapter.setVideosData(videoList);
            }
        }
    }

    /**
     * The background worker that executes the calls to the MovieDB service
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    private class FetchReviewsTask extends AsyncTask<String[], Void, List<Review>> {

        private final String TAG = FetchReviewsTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Review> doInBackground(String[]... params) {
            String method = params[0][0];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getMoviedbLanguageQueryParam(), MovieGridFragment.getMovieLocale());

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponseFromHttpUrl(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return MovieDbJsonUtilities.getReviewsListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Review> reviewList) {
            if (!(reviewList.isEmpty())) {
                mReviewsAdapter.setReviewsData(reviewList);
            }
        }
    }

}
