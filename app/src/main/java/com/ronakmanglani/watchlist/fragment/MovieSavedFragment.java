package com.ronakmanglani.watchlist.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.adapter.MovieCursorAdapter;
import com.ronakmanglani.watchlist.adapter.MovieCursorAdapter.OnMovieClickListener;
import com.ronakmanglani.watchlist.database.MovieColumns;
import com.ronakmanglani.watchlist.database.MovieProvider;
import com.ronakmanglani.watchlist.widget.ItemPaddingDecoration;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;

public class MovieSavedFragment extends Fragment implements OnMovieClickListener, LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 42;

    private Context context;
    private MovieCursorAdapter adapter;
    private GridLayoutManager layoutManager;

    private int viewType;
    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Bind(R.id.no_results)              View noResults;
    @Bind(R.id.no_results_message)      TextView noResultsMessage;
    @Bind(R.id.progress_circle)         View progressCircle;
    @Bind(R.id.movie_grid)              RecyclerView recyclerView;

    // Fragment life cycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_saved, container, false);
        ButterKnife.bind(this, v);
        setRetainInstance(true);

        // Initialize variable
        context = getContext();
        viewType = getArguments().getInt(Watchlist.VIEW_TYPE);

        // Setup RecyclerView
        adapter = new MovieCursorAdapter(context, this, null);
        layoutManager = new GridLayoutManager(context, getNumberOfColumns());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ItemPaddingDecoration(context, R.dimen.recycler_item_padding));
        recyclerView.setAdapter(adapter);

        // Load movies from database
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return v;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    // Cursor Loader
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case CURSOR_LOADER_ID:
                // Returns a new CursorLoader
                Uri contentUri;
                if (viewType == Watchlist.VIEW_TYPE_WATCHED) {
                    contentUri = MovieProvider.Watched.CONTENT_URI;
                } else {
                    contentUri = MovieProvider.ToSee.CONTENT_URI;
                }
                return new CursorLoader(context, contentUri,
                        new String[]{ MovieColumns._ID, MovieColumns.TMDB_ID, MovieColumns.TITLE, MovieColumns.YEAR,
                                MovieColumns.OVERVIEW, MovieColumns.RATING, MovieColumns.POSTER, MovieColumns.BACKDROP},
                        null, null, null);
            default:
                // An invalid id was passed in
                return null;
        }
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            progressCircle.setVisibility(View.GONE);
            noResults.setVisibility(View.VISIBLE);
            noResultsMessage.setText(R.string.saved_empty);
        } else {
            progressCircle.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.swapCursor(data);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    // Helper methods
    public void refreshLayout() {
        Parcelable state = layoutManager.onSaveInstanceState();
        layoutManager = new GridLayoutManager(getContext(), getNumberOfColumns());
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.onRestoreInstanceState(state);
    }
    public int getNumberOfColumns() {
        // Get screen width
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPx = displayMetrics.widthPixels;
        if (isTablet) {
            widthPx = widthPx / 3;
        }
        // Calculate desired width
        SharedPreferences preferences = context.getSharedPreferences(Watchlist.TABLE_USER, Context.MODE_PRIVATE);
        if (preferences.getInt(Watchlist.VIEW_MODE, Watchlist.VIEW_MODE_GRID) == Watchlist.VIEW_MODE_GRID) {
            float desiredPx = getResources().getDimensionPixelSize(R.dimen.movie_card_width);
            int columns = Math.round(widthPx / desiredPx);
            return columns > 2 ? columns : 2;
        } else {
            float desiredPx = getResources().getDimensionPixelSize(R.dimen.movie_detail_card_width);
            int columns = Math.round(widthPx / desiredPx);
            return columns > 1 ? columns : 1;
        }
    }

    // Click events
    @Override
    public void onMovieClicked(int position) {
        // TODO: Handle clicks
    }
}
