package com.ronakmanglani.watchlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Review;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReviewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Review> reviewList;                         // List of movies to be displayed
    private final OnReviewClickListener onReviewClickListener;   // Click listener for movie item

    // Constructor
    public ReviewsAdapter(ArrayList<Review> reviewList, OnReviewClickListener onReviewClickListener) {
        this.reviewList = reviewList;
        this.onReviewClickListener = onReviewClickListener;
    }

    // Return size of ArrayList
    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    // Inflate layout and fill data
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v, onReviewClickListener);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Get review
        Review review = reviewList.get(position);
        // Set attributes to ViewHolder
        ReviewViewHolder holder = (ReviewViewHolder) viewHolder;
        holder.reviewAuthor.setText(review.author);
        holder.reviewBody.setText(review.body);
    }

    // ViewHolder for Reviews
    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.review_item) View reviewItem;
        @Bind(R.id.review_author) TextView reviewAuthor;
        @Bind(R.id.review_body) TextView reviewBody;

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

    // Interface to respond to clicks
    public interface OnReviewClickListener {
        void onReviewClicked(final int position);
    }
}