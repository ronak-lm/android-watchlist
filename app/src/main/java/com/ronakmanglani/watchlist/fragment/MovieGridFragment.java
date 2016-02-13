package com.ronakmanglani.watchlist.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.ronakmanglani.watchlist.Watchlist;
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

    private Context context;
    private int viewType;
    private boolean isLoading;
    private boolean isLoadingLocked;

    private int pageToDownload;
    private static final int TOTAL_PAGES = 999;

    private MovieAdapter adapter;
    private GridLayoutManager layoutManager;

    @BindBool(R.bool.is_tablet) boolean isTablet;
    @Bind(R.id.error_message) View errorMessage;
    @Bind(R.id.progress_circle) View progressCircle;
    @Bind(R.id.loading_more) View loadingMore;
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.movie_grid) RecyclerView recyclerView;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_grid,container,false);
        context = getContext();
        ButterKnife.bind(this, v);

        // Initialize variables
        pageToDownload = 1;
        viewType = getArguments().getInt(Watchlist.VIEW_TYPE);

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
                // Load more if RecyclerView has reached the end and isn't already loading
                if (layoutManager.findLastVisibleItemPosition() == adapter.movieList.size() - 1 && !isLoadingLocked && !isLoading) {
                    if (pageToDownload < TOTAL_PAGES) {
                        loadingMore.setVisibility(View.VISIBLE);
                        downloadMoviesList();
                    }
                }
            }
        });

        // Setup swipe refresh
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Toggle visibility
                errorMessage.setVisibility(View.GONE);
                progressCircle.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                // Remove cache
                VolleySingleton.getInstance(context).requestQueue.getCache().remove(getUrlToDownload(1));
                // Download again
                pageToDownload = 1;
                adapter = null;
                downloadMoviesList();
            }
        });

        // Get the movies list
        if (savedInstanceState == null || !savedInstanceState.containsKey(Watchlist.MOVIE_LIST)) {
            downloadMoviesList();
        } else {
            adapter.movieList = savedInstanceState.getParcelableArrayList(Watchlist.MOVIE_LIST);
            pageToDownload = savedInstanceState.getInt(Watchlist.PAGE_TO_DOWNLOAD);
            isLoadingLocked = savedInstanceState.getBoolean(Watchlist.IS_LOCKED);
            isLoading = savedInstanceState.getBoolean(Watchlist.IS_LOADING);
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
        }

        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (layoutManager != null && adapter != null) {
            outState.putBoolean(Watchlist.IS_LOADING, isLoading);
            outState.putBoolean(Watchlist.IS_LOCKED, isLoadingLocked);
            outState.putInt(Watchlist.PAGE_TO_DOWNLOAD, pageToDownload);
            outState.putParcelableArrayList(Watchlist.MOVIE_LIST, adapter.movieList);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VolleySingleton.getInstance(context).requestQueue.cancelAll(this.getClass().getName());
        ButterKnife.unbind(this);
    }

    // JSON parsing and display
    public String getUrlToDownload(int page) {
        if (viewType == Watchlist.VIEW_TYPE_POPULAR) {
            return TMDBHelper.getMostPopularMoviesLink(getActivity(), page);
        } else if (viewType == Watchlist.VIEW_TYPE_RATED) {
            return TMDBHelper.getHighestRatedMoviesLink(getActivity(), page);
        } else if (viewType == Watchlist.VIEW_TYPE_UPCOMING) {
            return TMDBHelper.getUpcomingMoviesLink(getActivity(), page);
        } else if (viewType == Watchlist.VIEW_TYPE_PLAYING) {
            return TMDBHelper.getNowPlayingMoviesLink(getActivity(), page);
        }
        return null;
    }
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
        // Get desired width
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
        adapter = null;
        downloadMoviesList();
    }
    @Override
    public void onMovieClicked(int position) {
        if (isTablet) {
            ((MovieActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(position).id);
        } else {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra(Watchlist.MOVIE_ID, adapter.movieList.get(position).id);
            startActivity(intent);
        }
    }
}
