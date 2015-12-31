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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.util.APIHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import java.util.ArrayList;

public class MainRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;                                    // Context of calling activity
    private SharedPreferences sharedPref;                       // Application's SharedPreferences
    public ArrayList<Movie> movieList;                     // List of movies to be displayed
    private final OnItemClickListener onItemClickListener;      // Click Listener
    private int imageWidth;                                     // Width of the CardView (in pixels)

    // Constructor
    public MainRecyclerAdapter(Context context, OnItemClickListener onItemClickListener) {
        // Initialize members
        this.context = context;
        this.movieList = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Load CardView image width
        imageWidth = sharedPref.getInt(context.getString(R.string.settings_thumbnail_image_width), 0);
    }

    // Returns size of ArrayList
    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // Inflate Layout
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate layout
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
        return new MovieViewHolder(v, onItemClickListener);
    }

    // Insert data into the layout
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Get MovieThumb object
        Movie movie = movieList.get(position);
        // Set attributes to ViewHolder objects
        MovieViewHolder movieViewHolder = (MovieViewHolder) viewHolder;
        movieViewHolder.movieName.setText(movie.name);
        movieViewHolder.releaseYear.setText(movie.year);
        // Set movie poster
        String imageURL = APIHelper.getImageURL(movie.backdropImage, imageWidth);
        if (imageURL.endsWith("null")) {
            movieViewHolder.defaultImageView.setVisibility(View.VISIBLE);
            movieViewHolder.imageView.setVisibility(View.GONE);
        } else {
            movieViewHolder.imageView.setVisibility(View.VISIBLE);
            movieViewHolder.defaultImageView.setVisibility(View.GONE);
            movieViewHolder.imageView.setImageUrl(imageURL, VolleySingleton.getInstance(context).imageLoader);
        }
    }

    // Interface to respond to clicks
    public interface OnItemClickListener {
        void onCardClicked(final int position);
        void onMenuClicked(final int position, ImageButton editButton);
    }

    // ViewHolder for the layout
    public class MovieViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView defaultImageView;
        final NetworkImageView imageView;
        final TextView movieName;
        final TextView releaseYear;
        final ImageButton menuButton;

        public MovieViewHolder(final ViewGroup itemView, final OnItemClickListener onItemClickListener)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.movie_card);
            defaultImageView = (ImageView) itemView.findViewById(R.id.movie_poster_default);
            imageView = (NetworkImageView) itemView.findViewById(R.id.movie_poster);
            movieName = (TextView) itemView.findViewById(R.id.movie_title);
            releaseYear = (TextView) itemView.findViewById(R.id.movie_year);
            menuButton = (ImageButton) itemView.findViewById(R.id.movie_menu);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onCardClicked(getAdapterPosition());
                }
            });

            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onMenuClicked(getAdapterPosition(), menuButton);
                }
            });
        }
    }
}