package com.ronakmanglani.watchlist.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Review;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Review> reviewList;
    private final OnReviewClickListener onReviewClickListener;

    // Constructor
    public ReviewAdapter(ArrayList<Review> reviewList, OnReviewClickListener onReviewClickListener) {
        this.reviewList = reviewList;
        this.onReviewClickListener = onReviewClickListener;
    }

    // RecyclerView methods
    @Override
    public int getItemCount() {
        return reviewList.size();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v, onReviewClickListener);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Review review = reviewList.get(position);
        ReviewViewHolder holder = (ReviewViewHolder) viewHolder;
        holder.reviewAuthor.setText(review.userName);
        if (review.hasSpoiler) {
            holder.reviewBody.setVisibility(View.GONE);
            holder.reviewSpoiler.setVisibility(View.VISIBLE);
        } else {
            holder.reviewBody.setText(review.comment);
            holder.reviewBody.setVisibility(View.VISIBLE);
            holder.reviewSpoiler.setVisibility(View.GONE);
        }
        holder.reviewTime.setText(review.createdAt);
    }

    // ViewHolder
    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.review_item)         View reviewItem;
        @BindView(R.id.review_author)       TextView reviewAuthor;
        @BindView(R.id.review_body)         TextView reviewBody;
        @BindView(R.id.review_spoiler)      TextView reviewSpoiler;
        @BindView(R.id.review_time)         TextView reviewTime;

        public ReviewViewHolder(final ViewGroup itemView, final OnReviewClickListener onReviewClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            reviewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onReviewClickListener.onReviewClicked(getAdapterPosition());
                }
            });
        }
    }

    // Click listener interface
    public interface OnReviewClickListener {
        void onReviewClicked(final int position);
    }
}