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
import com.ronakmanglani.watchlist.util.APIHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.widget.AutoResizeTextView;

import java.util.ArrayList;

public class MainRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;                                    // Context of calling activity
    private SharedPreferences sharedPref;                       // Application's SharedPreferences
    public ArrayList<Movie> movieList;                          // List of movies to be displayed
    private final OnItemClickListener onItemClickListener;      // Click Listener
    private int imageWidth;                                     // Width of the CardView (in pixels)
    private int spanLocation;                                   // Flag to decide which view's to be detailed
    private boolean isDetailedViewEnabled;                      // Flag to enable/disable detailed layout

    // Constructor
    public MainRecyclerAdapter(Context context, OnItemClickListener onItemClickListener,
                               boolean isDetailedViewEnabled, int spanLocation) {
        // Initialize members
        this.context = context;
        this.movieList = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        this.isDetailedViewEnabled = isDetailedViewEnabled;
        this.spanLocation = spanLocation;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Load CardView image width
        imageWidth = sharedPref.getInt(context.getString(R.string.settings_thumbnail_image_width), 0);
    }

    // Returns size of ArrayList
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

    // Inflate Layout
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            // Inflate detailed layout
            ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_detail, parent, false);
            return new MovieDetailViewHolder(v, onItemClickListener);
        } else {
            // Inflate basic layout
            final ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
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
            return new MovieBasicViewHolder(v, onItemClickListener);
        }
    }

    // Insert data into the layout
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
                int imageSize = (int)context.getResources().getDimension(R.dimen.movie_detail_poster_width);
                String imageUrl = APIHelper.getImageURL(movie.posterImage, imageSize);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(context).imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            }
            movieViewHolder.movieName.setText(movie.title);
            movieViewHolder.movieRating.setText(movie.rating);
            movieViewHolder.releaseYear.setText(movie.year);
            movieViewHolder.overview.setText(movie.overview);
        } else {
            // Basic view
            MovieBasicViewHolder movieViewHolder = (MovieBasicViewHolder) viewHolder;
            if (movie.backdropImage != null && !movie.backdropImage.equals("null")) {
                String imageUrl = APIHelper.getImageURL(movie.backdropImage, imageWidth);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(context).imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            } else if (movie.posterImage != null && !movie.posterImage.equals("null")) {
                String imageUrl = APIHelper.getImageURL(movie.posterImage, imageWidth);
                movieViewHolder.imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(context).imageLoader);
                movieViewHolder.imageView.setVisibility(View.VISIBLE);
                movieViewHolder.defaultImageView.setVisibility(View.GONE);
            } else {
                movieViewHolder.defaultImageView.setVisibility(View.VISIBLE);
                movieViewHolder.imageView.setVisibility(View.GONE);
            }
            movieViewHolder.movieName.setText(movie.title);
            movieViewHolder.releaseYear.setText(movie.year);
            if (movie.rating == null || movie.rating.equals("0")) {
                movieViewHolder.movieRatingIcon.setVisibility(View.GONE);
                movieViewHolder.movieRating.setVisibility(View.GONE);
            } else {
                movieViewHolder.movieRatingIcon.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setVisibility(View.VISIBLE);
                movieViewHolder.movieRating.setText(movie.rating);
            }
        }
    }

    // Interface to respond to clicks
    public interface OnItemClickListener {
        void onCardClicked(final int position);
    }

    // ViewHolder for the layout
    public class MovieBasicViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView defaultImageView;
        final NetworkImageView imageView;
        final TextView movieName;
        final TextView movieRating;
        final ImageView movieRatingIcon;
        final TextView releaseYear;

        public MovieBasicViewHolder(final ViewGroup itemView, final OnItemClickListener onItemClickListener)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.movie_card);
            defaultImageView = (ImageView) itemView.findViewById(R.id.movie_poster_default);
            imageView = (NetworkImageView) itemView.findViewById(R.id.movie_poster);
            movieName = (TextView) itemView.findViewById(R.id.movie_title);
            movieRating = (TextView) itemView.findViewById(R.id.movie_rating);
            movieRatingIcon = (ImageView) itemView.findViewById(R.id.rating_icon);
            releaseYear = (TextView) itemView.findViewById(R.id.movie_year);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onCardClicked(getAdapterPosition());
                }
            });
        }
    }

    // ViewHolder for the layout
    public class MovieDetailViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView defaultImageView;
        final NetworkImageView imageView;
        final TextView movieName;
        final TextView movieRating;
        final TextView releaseYear;
        final AutoResizeTextView overview;

        public MovieDetailViewHolder(final ViewGroup itemView, final OnItemClickListener onItemClickListener)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.movie_card);
            defaultImageView = (ImageView) itemView.findViewById(R.id.movie_poster_default);
            imageView = (NetworkImageView) itemView.findViewById(R.id.movie_poster);
            movieName = (TextView) itemView.findViewById(R.id.movie_title);
            movieRating = (TextView) itemView.findViewById(R.id.movie_rating);
            releaseYear = (TextView) itemView.findViewById(R.id.movie_year);
            overview = (AutoResizeTextView) itemView.findViewById(R.id.movie_overview);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onCardClicked(getAdapterPosition());
                }
            });
        }
    }
}