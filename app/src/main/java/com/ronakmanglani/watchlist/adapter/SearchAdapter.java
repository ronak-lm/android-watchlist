package com.ronakmanglani.watchlist.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.util.ApiHelper;
import com.ronakmanglani.watchlist.util.TextUtils;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    public ArrayList<Movie> movieList;
    private final OnMovieClickListener onMovieClickListener;

    // Constructor
    public SearchAdapter(Context context, OnMovieClickListener onMovieClickListener) {
        this.context = context;
        this.movieList = new ArrayList<>();
        this.onMovieClickListener = onMovieClickListener;
    }

    // RecyclerView methods
    @Override
    public int getItemCount() {
        return movieList.size();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_compact, parent, false);
        return new SearchViewHolder(v, onMovieClickListener);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Movie movie = movieList.get(position);
        final SearchViewHolder holder = (SearchViewHolder) viewHolder;
        // Load image
        if (TextUtils.isNullOrEmpty(movie.backdropImage)) {
            holder.movieImage.setImageResource(R.drawable.default_backdrop_circle);
        } else {
            int imageSize = (int) context.getResources().getDimension(R.dimen.movie_compact_image_size);
            String imageUrl = ApiHelper.getImageURL(movie.backdropImage, imageSize);
            VolleySingleton.getInstance(context).imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    holder.movieImage.setImageBitmap(imageContainer.getBitmap());
                }
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    holder.movieImage.setImageResource(R.drawable.default_backdrop_circle);
                }
            });
        }
        // Set text
        holder.movieName.setText(movie.title);
        holder.movieYear.setText(movie.year);
        if (TextUtils.isNullOrEmpty(movie.rating) || movie.rating.equals("0")) {
            holder.movieRatingHolder.setVisibility(View.GONE);
        } else {
            holder.movieRating.setText(movie.rating);
            holder.movieRatingHolder.setVisibility(View.VISIBLE);
        }
    }

    // ViewHolder
    public class SearchViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.movie_item)              View movieItem;
        @BindView(R.id.movie_image)             ImageView movieImage;
        @BindView(R.id.movie_name)              TextView movieName;
        @BindView(R.id.movie_year)              TextView movieYear;
        @BindView(R.id.movie_rating_holder)     View movieRatingHolder;
        @BindView(R.id.movie_rating)            TextView movieRating;

        public SearchViewHolder(final ViewGroup itemView, final OnMovieClickListener onMovieClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            movieItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMovieClickListener.onMovieClicked(getAdapterPosition());
                }
            });
        }
    }

    // Click listener interface
    public interface OnMovieClickListener {
        void onMovieClicked(final int position);
    }
}