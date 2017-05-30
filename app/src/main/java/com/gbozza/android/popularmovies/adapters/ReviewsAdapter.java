package com.gbozza.android.popularmovies.adapters;

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

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.models.Review;
import com.gbozza.android.popularmovies.utilities.SpannableUtilities;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to manage the Reviews RecyclerView in the Movie Detail Activity
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private List<Review> mReviewList;
    @BindString(R.string.movie_detail_review_by) String mDetailReviewByLabel;

    /**
     * Inner class to represent the ViewHolder for the Adapter
     */
    class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_review_author) TextView mReviewAuthorTextView;
        @BindView(R.id.tv_review_content) TextView mReviewContentTextView;
        @BindView(R.id.iv_review_show_more) ImageView mReviewShowMoreImageView;
        Context mContext;

        /**
         * Constructor to the ViewHolder class
         *
         * @param view the we are going to inflate
         */
        ReviewsAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mContext = view.getContext();
        }
    }

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutIdForListItem = R.layout.reviews_view;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        ButterKnife.bind(this, view);
        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReviewsAdapterViewHolder reviewsAdapterViewHolder, int position) {
        Review review = mReviewList.get(position);
        reviewsAdapterViewHolder.mReviewAuthorTextView.append(SpannableUtilities.makeBold(mDetailReviewByLabel));
        reviewsAdapterViewHolder.mReviewAuthorTextView.append(review.getAuthor());
        reviewsAdapterViewHolder.mReviewContentTextView.setText(review.getContent());
        reviewsAdapterViewHolder.mReviewShowMoreImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int reviewContentLines = TextViewCompat.getMaxLines(reviewsAdapterViewHolder.mReviewContentTextView);
                int reviewCollapsedMaxLines = reviewsAdapterViewHolder.mContext
                        .getResources().getInteger(R.integer.review_collapsed_max_lines);
                if (reviewContentLines != reviewCollapsedMaxLines) {
                    reviewsAdapterViewHolder.mReviewContentTextView.setMaxLines(reviewCollapsedMaxLines);
                    reviewsAdapterViewHolder.mReviewShowMoreImageView.setImageResource(R.drawable.ic_expand_more);
                } else {
                    ObjectAnimator animation = ObjectAnimator.ofInt(
                            reviewsAdapterViewHolder.mReviewContentTextView,
                            "maxLines",
                            9999);
                    animation.setDuration(1000).start();
                    reviewsAdapterViewHolder.mReviewShowMoreImageView.setImageResource(R.drawable.ic_expand_less);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mReviewList) return 0;
        return mReviewList.size();
    }

    /**
     * Setter method for the Review List Object
     *
     * @param reviewList the List containing Review Objects
     */
    public void setReviewsData(List<Review> reviewList) {
        if (null == mReviewList) {
            mReviewList = reviewList;
        } else {
            mReviewList.addAll(reviewList);
        }
        notifyDataSetChanged();
    }

    /**
     * Getter method for the Review List Object
     *
     * @return the Review List
     */
    public List<Review> getReviewsData() {
        return mReviewList;
    }

}
