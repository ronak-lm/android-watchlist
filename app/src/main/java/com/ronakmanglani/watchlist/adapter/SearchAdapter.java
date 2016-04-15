package com.ronakmanglani.watchlist.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.model.Review;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

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
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new SearchViewHolder(v, onMovieClickListener);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Movie movie = movieList.get(position);
        final SearchViewHolder holder = (SearchViewHolder) viewHolder;
        // Load image
        int imageSize = (int) context.getResources().getDimension(R.dimen.search_image_size);
        String imageUrl = TMDBHelper.getImageURL(movie.backdropImage, imageSize);
        VolleySingleton.getInstance(context).imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                holder.movieImage.setImageBitmap(imageContainer.getBitmap());
            }
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // Do nothing
            }
        });
        // Set text
        holder.movieName.setText(movie.title);
        holder.movieYear.setText(movie.year);
    }

    // ViewHolder
    public class SearchViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.movie_item)              View movieItem;
        @Bind(R.id.movie_image)             CircleImageView movieImage;
        @Bind(R.id.movie_name)              TextView movieName;
        @Bind(R.id.movie_year)              TextView movieYear;

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