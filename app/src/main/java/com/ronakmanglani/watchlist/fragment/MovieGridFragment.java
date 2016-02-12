package com.ronakmanglani.watchlist.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.activity.MovieDetailActivity;
import com.ronakmanglani.watchlist.activity.MovieActivity;
import com.ronakmanglani.watchlist.adapter.MovieAdapter;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.widget.ItemPaddingDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieGridFragment extends Fragment implements MovieAdapter.OnMovieClickListener {

    // Constants for bundle arguments
    public static final String VIEW_TYPE_KEY = "view_type";
    public static final int VIEW_TYPE_POPULAR = 1;
    public static final int VIEW_TYPE_RATED = 2;
    public static final int VIEW_TYPE_UPCOMING = 3;
    public static final int VIEW_TYPE_PLAYING = 4;

    // Page counters
    private int pageToDownload;                     // Page number to download
    private static final int TOTAL_PAGES = 999;     // Total pages that can be downloaded

    private Context context;                        // Activity context
    private int viewType;                           // Type of movies to show
    private boolean isLoading;                      // Flag for loading
    private boolean isLoadingLocked;                // Flag to lock loading more data

    // Flag for two pane mode
    @BindBool(R.bool.is_tablet) boolean isTablet;

    // Layout views
    @Bind(R.id.error_message) View errorMessage;
    @Bind(R.id.progress_circle) View progressCircle;
    @Bind(R.id.loading_more) View loadingMore;
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.movie_grid) RecyclerView recyclerView;

    // Adapter and layout manager for RecyclerView
    private MovieAdapter adapter;
    private GridLayoutManager layoutManager;

    // Abstract methods
    public String getUrlToDownload(int page) {
        if (viewType == VIEW_TYPE_POPULAR) {
            return TMDBHelper.getMostPopularMoviesLink(getActivity(), page);
        } else if (viewType == VIEW_TYPE_RATED) {
            return TMDBHelper.getHighestRatedMoviesLink(getActivity(), page);
        } else if (viewType == VIEW_TYPE_UPCOMING) {
            return TMDBHelper.getUpcomingMoviesLink(getActivity(), page);
        } else if (viewType == VIEW_TYPE_PLAYING) {
            return TMDBHelper.getNowPlayingMoviesLink(getActivity(), page);
        } else {
            return "";
        }
    }

    // Fragment lifecycle methods
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_grid,container,false);
        context = getContext();
        ButterKnife.bind(this, v);

        // Initialize variables
        pageToDownload = 1;
        viewType = getArguments().getInt(VIEW_TYPE_KEY);

        // Setup RecyclerView
        adapter = new MovieAdapter(context, this);
        layoutManager = new GridLayoutManager(context, getNumberOfColumns());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ItemPaddingDecoration(context, R.dimen.recycler_item_padding));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Check if RecyclerView has reached the end and isn't already loading
                if (layoutManager.findLastVisibleItemPosition() == adapter.movieList.size() - 1 && !isLoadingLocked && !isLoading) {
                    if (pageToDownload < TOTAL_PAGES) {
                        loadingMore.setVisibility(View.VISIBLE);
                        downloadMoviesList();
                    }
                }
            }
        });

        // Setup swipe to refresh
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide all views
                errorMessage.setVisibility(View.GONE);
                progressCircle.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                // Invalidate current adapter
                adapter = null;
                // Invalidate cache
                VolleySingleton.getInstance(context).requestQueue.getCache().remove(getUrlToDownload(1));
                // Download refreshed data
                pageToDownload = 1;
                downloadMoviesList();
            }
        });

        // Get the movies list
        if (savedInstanceState == null) {
            downloadMoviesList();
        } else {
            if (savedInstanceState.containsKey("movie_list")) {
                // Restore data from bundle
                adapter.movieList = savedInstanceState.getParcelableArrayList("movie_list");
                pageToDownload = savedInstanceState.getInt("page_to_download");
                isLoadingLocked = savedInstanceState.getBoolean("is_locked");
                isLoading = savedInstanceState.getBoolean("is_loading");
                // Continue download if stopped, else show list
                if (isLoading) {
                    if (pageToDownload == 1) {
                        progressCircle.setVisibility(View.VISIBLE);
                        loadingMore.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                    } else {
                        progressCircle.setVisibility(View.GONE);
                        loadingMore.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                    }
                    downloadMoviesList();
                } else {
                    onDownloadSuccessful();
                }
            } else {
                // Data not found, download from TMDB
                downloadMoviesList();
            }
        }

        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Persist changes when fragment is destroyed
        if (layoutManager != null && adapter != null) {
            outState.putBoolean("is_loading", isLoading);
            outState.putBoolean("is_locked", isLoadingLocked);
            outState.putInt("page_to_download", pageToDownload);
            outState.putParcelableArrayList("movie_list", adapter.movieList);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel any pending network requests
        VolleySingleton.getInstance(context).requestQueue.cancelAll(this.getClass().getName());
        // Unbind layout views
        ButterKnife.unbind(this);
    }

    // Network related methods
    private void downloadMoviesList() {
        // Select which URL to download
        String urlToDownload = getUrlToDownload(pageToDownload);
        // Create new adapter if it's null
        if (adapter == null) {
            adapter = new MovieAdapter(context, this);
            recyclerView.setAdapter(adapter);
        }
        // Set flag
        isLoading = true;
        // Make JSON Request
        final JsonObjectRequest request = new JsonObjectRequest (
                // Request method and URL to be downloaded
                Request.Method.GET, urlToDownload, null,
                // To respond when JSON gets downloaded
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            // Get the result and loop through it
                            JSONArray result = jsonObject.getJSONArray("results");
                            for (int i = 0; i < result.length(); i++) {
                                // Get movie object
                                JSONObject movie = (JSONObject) result.get(i);
                                // Get info from object
                                String poster = movie.getString("poster_path");
                                String overview = movie.getString("overview");
                                String year = movie.getString("release_date");
                                if (year != null && !year.equals("null")) {
                                    year = year.substring(0, 4);
                                }
                                String id = movie.getString("id");
                                String title = movie.getString("title");
                                String backdrop = movie.getString("backdrop_path");
                                String rating = movie.getString("vote_average");
                                // Create MovieThumb object and add to list
                                Movie thumb = new Movie(id, title, year, overview, rating, poster, backdrop);
                                adapter.movieList.add(thumb);
                            }
                            // Load first movie in fragment if in two-pane mode
                            if (pageToDownload == 1 && adapter.movieList.size() > 0 && isTablet) {
                                ((MovieActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(0).id);
                            }
                            // Set next page for download
                            pageToDownload++;
                            // Update UI
                            onDownloadSuccessful();
                        } catch (Exception ex) {
                            // To show error message on parsing errors
                            onDownloadFailed();
                        }
                    }
                },
                // To show error message on network errors
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onDownloadFailed();
                    }
                });

        // Set thread tags for reference
        request.setTag(this.getClass().getName());

        // Add download request to queue
        VolleySingleton.getInstance(context).requestQueue.add(request);
    }
    private void onDownloadSuccessful() {
        isLoading = false;
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.GONE);
        loadingMore.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(true);
        adapter.notifyDataSetChanged();
    }
    private void onDownloadFailed() {
        isLoading = false;
        if (pageToDownload == 1) {
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setVisibility(View.GONE);
            errorMessage.setVisibility(View.VISIBLE);
        } else {
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            errorMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
            isLoadingLocked = true;
        }
    }

    // Helper methods
    public int getNumberOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPx = displayMetrics.widthPixels;
        if (isTablet) {
            widthPx = widthPx / 3;
        }
        float desiredPx = getResources().getDimensionPixelSize(R.dimen.movie_card_width);
        int columns = Math.round(widthPx / desiredPx);
        return columns > 2 ? columns : 2;
    }

    // Click events
    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        // Hide all views
        errorMessage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setVisibility(View.GONE);
        // Show progress circle
        progressCircle.setVisibility(View.VISIBLE);
        // Try to download the data again
        pageToDownload = 1;
        downloadMoviesList();
    }
    @Override
    public void onMovieClicked(int position) {
        if (isTablet) {
            ((MovieActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(position).id);
        } else {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.MOVIE_ID, adapter.movieList.get(position).id);
            startActivity(intent);
        }
    }
}
