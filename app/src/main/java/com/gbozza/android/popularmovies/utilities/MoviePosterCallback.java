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

import android.view.View;

import com.gbozza.android.popularmovies.adapters.MoviesAdapter;
import com.squareup.picasso.Callback;

/**
 * Extension of the Callback interface from Picasso
 */
public class MoviePosterCallback extends Callback.EmptyCallback {

    private MoviesAdapter.MoviesAdapterViewHolder mViewHolder;

    /**
     * Constructor
     *
     * @param viewHolder The ViewHolder of the class using this extension
     */
    public MoviePosterCallback(MoviesAdapter.MoviesAdapterViewHolder viewHolder) {
        this.mViewHolder = viewHolder;
    }

    @Override
    public void onSuccess() {
        mViewHolder.mMoviePosterProgressBar.setVisibility(View.GONE);
        mViewHolder.mMoviePosterErrorTextView.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        mViewHolder.mMoviePosterProgressBar.setVisibility(View.GONE);
        mViewHolder.mMoviePosterErrorTextView.setRotation(-20);
        mViewHolder.mMoviePosterErrorTextView.setVisibility(View.VISIBLE);
    }
}