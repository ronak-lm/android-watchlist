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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.activity.MovieActivity;
import com.ronakmanglani.watchlist.activity.MovieDetailActivity;
import com.ronakmanglani.watchlist.adapter.MovieAdapter;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.util.ApiHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.widget.ItemPaddingDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieListFragment extends Fragment implements MovieAdapter.OnMovieClickListener {

    private Context context;
    private Tracker tracker;

    private MovieAdapter adapter;
    private GridLayoutManager layoutManager;

    private int pageToDownload;
    private static final int TOTAL_PAGES = 999;

    private int viewType;
    private boolean isLoading;
    private boolean isLoadingLocked;
    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Bind(R.id.error_message)       View errorMessage;
    @Bind(R.id.progress_circle)     View progressCircle;
    @Bind(R.id.loading_more)        View loadingMore;
    @Bind(R.id.swipe_refresh)       SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.movie_grid)          RecyclerView recyclerView;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_list,container,false);
        context = getContext();
        ButterKnife.bind(this, v);

        // Initialize variables
        pageToDownload = 1;
        viewType = getArguments().getInt(Watchlist.VIEW_TYPE);
        tracker = ((Watchlist) getActivity().getApplication()).getTracker();

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
            // Download again if stopped, else show list
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
    public void onResume() {
        super.onResume();
        // Determine screen name
        String screenName;
        switch (viewType) {
            case Watchlist.VIEW_TYPE_POPULAR:
                screenName = getString(R.string.screen_popular);
                break;

            case Watchlist.VIEW_TYPE_RATED:
                screenName = getString(R.string.screen_rated);
                break;

            case Watchlist.VIEW_TYPE_UPCOMING:
                screenName = getString(R.string.screen_upcoming);
                break;

            case Watchlist.VIEW_TYPE_PLAYING:
                screenName = getString(R.string.screen_playing);
                break;

            default:
                screenName = getString(R.string.screen_movie_list);
                break;
        }
        // Send screen name to analytics
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        ButterKnife.unbind(this);
        VolleySingleton.getInstance(context).requestQueue.cancelAll(this.getClass().getName());
    }

    // JSON parsing and display
    public String getUrlToDownload(int page) {
        if (viewType == Watchlist.VIEW_TYPE_POPULAR) {
            return ApiHelper.getMostPopularMoviesLink(getActivity(), page);
        } else if (viewType == Watchlist.VIEW_TYPE_RATED) {
            return ApiHelper.getHighestRatedMoviesLink(getActivity(), page);
        } else if (viewType == Watchlist.VIEW_TYPE_UPCOMING) {
            return ApiHelper.getUpcomingMoviesLink(getActivity(), page);
        } else if (viewType == Watchlist.VIEW_TYPE_PLAYING) {
            return ApiHelper.getNowPlayingMoviesLink(getActivity(), page);
        }
        return null;
    }
    private void downloadMoviesList() {
        if (adapter == null) {
            adapter = new MovieAdapter(context, this);
            recyclerView.setAdapter(adapter);
        }
        String urlToDownload = getUrlToDownload(pageToDownload);
        final JsonObjectRequest request = new JsonObjectRequest (
                Request.Method.GET, urlToDownload, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            JSONArray result = jsonObject.getJSONArray("results");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject movie = (JSONObject) result.get(i);
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

                                Movie thumb = new Movie(id, title, year, overview, rating, poster, backdrop);
                                adapter.movieList.add(thumb);
                            }

                            // Load detail fragment if in tablet mode
                            if (isTablet && pageToDownload == 1 && adapter.movieList.size() > 0) {
                                ((MovieActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(0).id);
                            }

                            pageToDownload++;
                            onDownloadSuccessful();

                        } catch (Exception ex) {
                            // JSON parsing error
                            onDownloadFailed();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // Network error
                        onDownloadFailed();
                    }
                });
        isLoading = true;
        request.setTag(this.getClass().getName());
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
