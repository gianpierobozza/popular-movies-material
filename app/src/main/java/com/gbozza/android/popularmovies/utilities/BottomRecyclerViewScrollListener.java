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

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * A Listener for the bottom scroll functionality of loading more data from the service
 */
public abstract class BottomRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private int mVisibleThreshold = 3;
    private int mCurrentPage;
    private int mPreviousTotalItemCount = 0;
    private boolean mLoading = true;
    private int mStartingPageIndex = 1;

    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * Constructor for the Class
     *
     * @param layoutManager the layout manager the RecyclerView is using
     * @param page the page we're currently showing
     */
    protected BottomRecyclerViewScrollListener(GridLayoutManager layoutManager, int page) {
        this.mLayoutManager = layoutManager;
        mVisibleThreshold = mVisibleThreshold * layoutManager.getSpanCount();
        mCurrentPage = page;
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        // check if the user is scrolling down
        if (dy > 0) {
            int totalItemCount = mLayoutManager.getItemCount();
            int lastVisibleItemPosition = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();

            // If the total item count is zero and the previous isn't, assume the
            // list is invalidated and should be reset back to initial state
            if (totalItemCount < mPreviousTotalItemCount) {
                this.mCurrentPage = this.mStartingPageIndex;
                this.mPreviousTotalItemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.mLoading = true;
                }
            }
            // If it’s still loading, we check to see if the dataset count has
            // changed, if so we conclude it has finished loading and update the current page
            // number and total item count.
            if (mLoading && (totalItemCount > mPreviousTotalItemCount)) {
                mLoading = false;
                mPreviousTotalItemCount = totalItemCount;
            }

            // If it isn’t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            // threshold should reflect how many total columns there are too
            if (!mLoading && (lastVisibleItemPosition + mVisibleThreshold) > totalItemCount) {
                mCurrentPage++;
                onLoadMore(mCurrentPage, totalItemCount, view);
                mLoading = true;
            }
        }
    }

    /**
     * Called when performing new searches to reset the listener
     */
    public void resetState() {
        this.mCurrentPage = this.mStartingPageIndex;
        this.mPreviousTotalItemCount = 0;
        this.mLoading = true;
    }

    /**
     * Defines the process for actually loading more data based on page
     *
     * @param page the page we need to load
     * @param totalItemsCount the total number of items
     * @param view the view used
     */
    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);

}