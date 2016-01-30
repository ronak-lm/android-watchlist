package com.ronakmanglani.watchlist.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.widget.AutoResizeTextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BaseMovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;                                    // Context of calling activity
    private SharedPreferences sharedPref;                       // Application's SharedPreferences

    public ArrayList<Movie> movieList;                          // List of movies to be displayed
    private final OnMovieClickListener onMovieClickListener;      // Click listener for movie item

    private int imageWidth;                                     // Width of the CardView (in pixels)
    private int spanLocation;                                   // Flag to decide which view's to be detailed
    private boolean isDetailedViewEnabled;                      // Flag to enable/disable detailed layout

    // Constructor
    public BaseMovieAdapter(Context context, OnMovieClickListener onMovieClickListener,
                            boolean isDetailedViewEnabled, int spanLocation) {
        // Initialize members
        this.context = context;
        this.movieList = new ArrayList<>();
        this.onMovieClickListener = onMovieClickListener;
        this.isDetailedViewEnabled = isDetailedViewEnabled;
        this.spanLocation = spanLocation;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Load CardView image width
        imageWidth = sharedPref.getInt(context.getString(R.string.settings_thumbnail_image_width), 0);
    }

    // Return size of ArrayList
    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // Return type of item (Detail or basic)
    @Override
    public int getItemViewType(int position) {
        if ((position + 1) % 7 == spanLocation && isDetailedViewEnabled) {
            return 1;       // Detail item
        } else {
            return 0;       // Basic item
        }
    }

    // Inflate layout and fill data
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            // Inflate detailed layout
            ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_detail, parent, false);
            return new MovieDetailViewHolder(v, onMovieClickListener);
        } else {
            // Inflate basic layout
            final ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_basic, parent, false);
            // To measure and save width of ImageView (if API >= 11)
            ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Update width integer and save to storage for next use
                        int width = v.findViewById(R.id.movie_poster).getWidth();
                        if (width > imageWidth) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt(context.getString(R.string.settings_thumbnail_image_width), width);
                            editor.apply();
                        }
                        // Unregister LayoutListener
                        if (Build.VERSION.SDK_INT >= 16) {
                            v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                });
            }
            // Return ViewHolder
            return new MovieBasicViewHolder(v, onMovieClickListener);
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Get Movie object
        Movie movie = movieList.get(position);
        // Set attributes to ViewHolder objects
        if (getItemViewType(position) == 1) {
            // Detailed view
            MovieDetailViewHolder movieViewHolder = (MovieDetailViewHolder) viewHolder;
            if (movie.posterImage == null || movie.posterImage.equals("null")) {
                movieViewHolder.imageView.setVisibility(View.GONE);
                movieViewHolder.defaultImageView.setVisibility(View.VISIBLE);
            } else {
                int imageSize = (int) context.getResources().getDimension(R.dimen.movie_detail_poster_width);
                String imageUrl = TMDBHelper.getImageURL(movie.posterImage, imageSize);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(context).imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            }
            movieViewHolder.movieName.setText(movie.title);
            movieViewHolder.releaseYear.setText(movie.year);
            movieViewHolder.overview.setText(movie.overview);
            if (movie.rating == null || movie.rating.equals("null") || movie.rating.equals("0")) {
                movieViewHolder.movieRatingIcon.setVisibility(View.GONE);
                movieViewHolder.movieRating.setVisibility(View.GONE);
            } else {
                movieViewHolder.movieRatingIcon.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setText(movie.rating);
            }
        } else {
            // Basic view
            MovieBasicViewHolder movieViewHolder = (MovieBasicViewHolder) viewHolder;
            if (movie.backdropImage != null && !movie.backdropImage.equals("null")) {
                String imageUrl = TMDBHelper.getImageURL(movie.backdropImage, imageWidth);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(context).imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            } else if (movie.posterImage != null && !movie.posterImage.equals("null")) {
                String imageUrl = TMDBHelper.getImageURL(movie.posterImage, imageWidth);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(context).imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            } else {
                movieViewHolder.defaultImageView.setVisibility(View.VISIBLE);
                movieViewHolder.imageView.setVisibility(View.GONE);
            }
            movieViewHolder.movieName.setText(movie.title);
            movieViewHolder.releaseYear.setText(movie.year);
            if (movie.rating == null || movie.rating.equals("null") || movie.rating.equals("0")) {
                movieViewHolder.movieRatingIcon.setVisibility(View.GONE);
                movieViewHolder.movieRating.setVisibility(View.GONE);
            } else {
                movieViewHolder.movieRatingIcon.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setText(movie.rating);
            }
        }
    }

    // ViewHolders
    public class MovieBasicViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.movie_card) CardView cardView;
        @Bind(R.id.movie_poster_default) ImageView defaultImageView;
        @Bind(R.id.movie_poster) NetworkImageView imageView;
        @Bind(R.id.movie_title) TextView movieName;
        @Bind(R.id.movie_year) TextView releaseYear;
        @Bind(R.id.movie_rating) TextView movieRating;
        @Bind(R.id.rating_icon) ImageView movieRatingIcon;

        public MovieBasicViewHolder(final ViewGroup itemView, final OnMovieClickListener onMovieClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMovieClickListener.onMovieClicked(getAdapterPosition());
                }
            });
        }
    }
    public class MovieDetailViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.movie_card) CardView cardView;
        @Bind(R.id.movie_poster_default) ImageView defaultImageView;
        @Bind(R.id.movie_poster) NetworkImageView imageView;
        @Bind(R.id.movie_title) TextView movieName;
        @Bind(R.id.movie_year) TextView releaseYear;
        @Bind(R.id.movie_overview) AutoResizeTextView overview;
        @Bind(R.id.movie_rating) TextView movieRating;
        @Bind(R.id.rating_icon) ImageView movieRatingIcon;

        public MovieDetailViewHolder(final ViewGroup itemView, final OnMovieClickListener onMovieClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMovieClickListener.onMovieClicked(getAdapterPosition());
                }
            });
        }
    }

    // Interface to respond to clicks
    public interface OnMovieClickListener {
        void onMovieClicked(final int position);
    }
}