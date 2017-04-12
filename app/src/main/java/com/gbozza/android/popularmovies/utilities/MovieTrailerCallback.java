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

import com.gbozza.android.popularmovies.adapters.VideosAdapter;
import com.squareup.picasso.Callback;

/**
 * Extension of the Callback interface from Picasso
 */
public class MovieTrailerCallback extends Callback.EmptyCallback {

    private VideosAdapter.VideosAdapterViewHolder mViewHolder;

    /**
     * Constructor
     *
     * @param viewHolder The ViewHolder of the class using this extension
     */
    public MovieTrailerCallback(VideosAdapter.VideosAdapterViewHolder viewHolder) {
        this.mViewHolder = viewHolder;
    }

    @Override
    public void onSuccess() {
        mViewHolder.mMovieTrailerProgressBar.setVisibility(View.GONE);
        mViewHolder.mMovieTrailerThumbnailError.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        mViewHolder.mMovieTrailerProgressBar.setVisibility(View.GONE);
        mViewHolder.mMovieTrailerThumbnailError.setVisibility(View.VISIBLE);
    }
}