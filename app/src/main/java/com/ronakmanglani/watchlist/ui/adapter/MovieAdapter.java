package com.ronakmanglani.watchlist.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.WatchlistApp;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.api.ApiHelper;
import com.ronakmanglani.watchlist.util.TextUtil;
import com.ronakmanglani.watchlist.api.VolleySingleton;
import com.ronakmanglani.watchlist.ui.view.AutoResizeTextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int imageWidth;
    private SharedPreferences sharedPref;

    public ArrayList<Movie> movieList;
    private final OnMovieClickListener onMovieClickListener;

    // Constructor
    public MovieAdapter(Context context, OnMovieClickListener onMovieClickListener) {
        this.context = context;
        this.movieList = new ArrayList<>();
        this.onMovieClickListener = onMovieClickListener;
        sharedPref = context.getSharedPreferences(WatchlistApp.TABLE_USER, Context.MODE_PRIVATE);
        imageWidth = sharedPref.getInt(WatchlistApp.THUMBNAIL_SIZE, 0);   // Load image width for grid view
    }

    // RecyclerView methods
    @Override
    public int getItemCount() {
        return movieList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return (sharedPref.getInt(WatchlistApp.VIEW_MODE, WatchlistApp.VIEW_MODE_GRID));
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == WatchlistApp.VIEW_MODE_GRID) {
            // GRID MODE
            final ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_grid, parent, false);
            ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Update width integer and save to storage for next use
                        int width = v.findViewById(R.id.movie_poster).getWidth();
                        if (width > imageWidth) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt(WatchlistApp.THUMBNAIL_SIZE, width);
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
            return new MovieGridViewHolder(v, onMovieClickListener);
        } else if (viewType == WatchlistApp.VIEW_MODE_LIST)  {
            // LIST MODE
            ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_list, parent, false);
            return new MovieListViewHolder(v, onMovieClickListener);
        } else {
            // COMPACT MODE
            ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_compact, parent, false);
            return new MovieCompactViewHolder(v, onMovieClickListener);
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(0);
        Movie movie = movieList.get(position);
        if (viewType == WatchlistApp.VIEW_MODE_GRID) {
            // GRID MODE
            MovieGridViewHolder movieViewHolder = (MovieGridViewHolder) viewHolder;

            // Title and year
            movieViewHolder.movieName.setText(movie.title);
            movieViewHolder.releaseYear.setText(movie.year);
            // Load image
            if (!TextUtil.isNullOrEmpty(movie.backdropImage)) {
                String imageUrl = ApiHelper.getImageURL(movie.backdropImage, imageWidth);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance().imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            } else if (!TextUtil.isNullOrEmpty(movie.posterImage)) {
                String imageUrl = ApiHelper.getImageURL(movie.posterImage, imageWidth);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance().imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            } else {
                movieViewHolder.defaultImageView.setVisibility(View.VISIBLE);
                movieViewHolder.imageView.setVisibility(View.GONE);
            }
            // Display movie rating
            if (TextUtil.isNullOrEmpty(movie.rating) || movie.rating.equals("0")) {
                movieViewHolder.movieRatingIcon.setVisibility(View.GONE);
                movieViewHolder.movieRating.setVisibility(View.GONE);
            } else {
                movieViewHolder.movieRatingIcon.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setText(movie.rating);
            }
        } else if (viewType == WatchlistApp.VIEW_MODE_LIST) {
            // LIST MODE
            MovieListViewHolder movieViewHolder = (MovieListViewHolder) viewHolder;

            // Title, year and overview
            movieViewHolder.movieName.setText(movie.title);
            movieViewHolder.releaseYear.setText(movie.year);
            movieViewHolder.overview.setText(movie.overview);
            // Load image
            if (TextUtil.isNullOrEmpty(movie.posterImage)) {
                movieViewHolder.imageView.setVisibility(View.GONE);
                movieViewHolder.defaultImageView.setVisibility(View.VISIBLE);
            } else {
                int imageSize = (int) context.getResources().getDimension(R.dimen.movie_list_poster_width);
                String imageUrl = ApiHelper.getImageURL(movie.posterImage, imageSize);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance().imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            }
            // Display movie rating
            if (TextUtil.isNullOrEmpty(movie.rating) || movie.rating.equals("0")) {
                movieViewHolder.movieRatingIcon.setVisibility(View.GONE);
                movieViewHolder.movieRating.setVisibility(View.GONE);
            } else {
                movieViewHolder.movieRatingIcon.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setText(movie.rating);
            }
        } else {
            // COMPACT MODE
            final MovieCompactViewHolder movieViewHolder = (MovieCompactViewHolder) viewHolder;

            // Title and year
            movieViewHolder.movieName.setText(movie.title);
            movieViewHolder.movieYear.setText(movie.year);
            // Load image
            if (TextUtil.isNullOrEmpty(movie.backdropImage)) {
                movieViewHolder.movieImage.setImageResource(R.drawable.default_backdrop_circle);
            } else {
                int imageSize = (int) context.getResources().getDimension(R.dimen.movie_compact_image_size);
                String imageUrl = ApiHelper.getImageURL(movie.backdropImage, imageSize);
                VolleySingleton.getInstance().imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        movieViewHolder.movieImage.setImageBitmap(response.getBitmap());
                    }
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        movieViewHolder.movieImage.setImageResource(R.drawable.default_backdrop_circle);
                    }
                });
            }
            // Display movie rating
            if (TextUtil.isNullOrEmpty(movie.rating) || movie.rating.equals("0")) {
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
    public class MovieGridViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.movie_card)              CardView cardView;
        @BindView(R.id.movie_poster_default)    ImageView defaultImageView;
        @BindView(R.id.movie_poster)            NetworkImageView imageView;
        @BindView(R.id.movie_title)             TextView movieName;
        @BindView(R.id.movie_year)              TextView releaseYear;
        @BindView(R.id.movie_rating)            TextView movieRating;
        @BindView(R.id.rating_icon)             ImageView movieRatingIcon;

        public MovieGridViewHolder(final ViewGroup itemView, final OnMovieClickListener onMovieClickListener) {
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
    public class MovieListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.movie_card)              CardView cardView;
        @BindView(R.id.movie_poster_default)    ImageView defaultImageView;
        @BindView(R.id.movie_poster)            NetworkImageView imageView;
        @BindView(R.id.movie_title)             TextView movieName;
        @BindView(R.id.movie_year)              TextView releaseYear;
        @BindView(R.id.movie_overview)          AutoResizeTextView overview;
        @BindView(R.id.movie_rating)            TextView movieRating;
        @BindView(R.id.rating_icon)             ImageView movieRatingIcon;

        public MovieListViewHolder(final ViewGroup itemView, final OnMovieClickListener onMovieClickListener) {
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
    public class MovieCompactViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.movie_item)              View movieItem;
        @BindView(R.id.movie_image)             ImageView movieImage;
        @BindView(R.id.movie_name)              TextView movieName;
        @BindView(R.id.movie_year)              TextView movieYear;
        @BindView(R.id.movie_rating)            TextView movieRating;
        @BindView(R.id.rating_icon)             ImageView movieRatingIcon;

        public MovieCompactViewHolder(final ViewGroup itemView, final OnMovieClickListener onMovieClickListener) {
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