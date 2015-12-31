package com.ronakmanglani.watchlist.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.ronakmanglani.watchlist.activity.MovieActivity;
import com.ronakmanglani.watchlist.adapter.MainRecyclerAdapter;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class MainBaseFragment extends Fragment {

    private static final int TOTAL_PAGES = 999;     // Total pages that can be downloaded

    private Context context;                        // Activity context
    private int pageToDownload;                     // Page number to download
    private boolean isLoading;                      // Flag for loading

    // Views for reference
    private View errorMessage;
    private View progressCircle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MainRecyclerAdapter adapter;
    private GridLayoutManager layoutManager;

    // Abstract methods
    public abstract String getUrlToDownload(int page);
    public abstract boolean isDetailedViewEnabled();

    // Fragment Initialization
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main,container,false);
        context = getContext();

        // Initialize count
        pageToDownload = 1;

        // Find and initialize views
        errorMessage = v.findViewById(R.id.error_message);
        progressCircle = v.findViewById(R.id.progress_circle);
        layoutManager = new GridLayoutManager(context, getNumberOfColumns());
        adapter = new MainRecyclerAdapter(context, onClickListener, isDetailedViewEnabled());
        recyclerView = (RecyclerView) v.findViewById(R.id.movie_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);

        // Set spanning for RecyclerView items if detailed view is enabled
        if (isDetailedViewEnabled()) {
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if ((position + 1) % 7 == 0) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            });
        }

        // Set listeners
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Check if RecyclerView has reached the end and isn't already loading
                if (layoutManager.findLastCompletelyVisibleItemPosition() == adapter.movieList.size() - 1 && !isLoading) {
                    // Set flag
                    isLoading = true;
                    // Check if page to download is less than total number of pages
                    if (pageToDownload < TOTAL_PAGES) {
                        // Show loading circle
                        swipeRefreshLayout.setRefreshing(true);
                        swipeRefreshLayout.setEnabled(false);
                        // Download the next page
                        downloadMoviesList();
                    }
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide all views
                errorMessage.setVisibility(View.GONE);
                progressCircle.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                // Invalidate cache
                VolleySingleton.getInstance(context).requestQueue.getCache().remove(getUrlToDownload(1));
                // Download refreshed data
                pageToDownload = 1;
                downloadMoviesList();
            }
        });
        errorMessage.findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        // Get the movies list
        if (savedInstanceState == null) {
            downloadMoviesList();
        } else {
            if (savedInstanceState.containsKey("movieList") && savedInstanceState.containsKey("layoutManagerState")) {
                // Restore data from bundle
                adapter.movieList = savedInstanceState.getParcelableArrayList("movieList");
                adapter.notifyDataSetChanged();
                pageToDownload = savedInstanceState.getInt("pageToDownload");
                layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("layoutManagerState"));
                // Update UI
                errorMessage.setVisibility(View.GONE);
                progressCircle.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);
            } else {
                // Data not found
                downloadMoviesList();
            }
        }

        return v;
    }
    // Returns the number of columns to display in the RecyclerView
    public int getNumberOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPx = displayMetrics.widthPixels;
        float desiredPx = getResources().getDimensionPixelSize(R.dimen.movie_card_width);
        int columns = Math.round(widthPx / desiredPx);
        return columns > 2 ? columns : 2;
    }

    // Persist changes when fragment is destroyed
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (layoutManager != null && adapter != null) {
            outState.putInt("pageToDownload", pageToDownload);
            outState.putParcelable("layoutManagerState", layoutManager.onSaveInstanceState());
            outState.putParcelableArrayList("movieList", adapter.movieList);
        }
    }

    // Cancel any pending network requests when fragment stops
    @Override
    public void onStop() {
        super.onStop();
        VolleySingleton.getInstance(context).requestQueue.cancelAll(this.getClass().getName());
    }

    // Download JSON data from TMDB
    private void downloadMoviesList() {
        // Select which URL to download
        String urlToDownload = getUrlToDownload(pageToDownload);

        // Create new adapter if first time
        if (adapter == null) {
            adapter = new MainRecyclerAdapter(context, onClickListener, isDetailedViewEnabled());
            recyclerView.setAdapter(adapter);
        }

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

                            // Update UI
                            errorMessage.setVisibility(View.GONE);
                            progressCircle.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setRefreshing(true);
                            swipeRefreshLayout.setEnabled(false);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                    swipeRefreshLayout.setEnabled(true);
                                }
                            }, 1000);
                            adapter.notifyDataSetChanged();

                            // Set next page for download
                            pageToDownload++;
                            isLoading = false;

                        } catch (Exception ex) {
                            // To show error message on parsing errors
                            showErrorMessage();
                        }
                    }
                },
                // To show error message on network errors
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showErrorMessage();
                    }
                });

        // Set thread tags for reference
        request.setTag(this.getClass().getName());

        // Add download request to queue
        VolleySingleton.getInstance(context).requestQueue.add(request);
    }
    // Show error message when download failed
    private void showErrorMessage() {
        adapter.movieList.clear();
        progressCircle.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    // Respond to clicks of items in RecyclerView
    MainRecyclerAdapter.OnItemClickListener onClickListener = new MainRecyclerAdapter.OnItemClickListener() {
        @Override
        public void onCardClicked(int position) {
            Intent intent = new Intent(context, MovieActivity.class);
            intent.putExtra(MovieActivity.MOVIE_ID, adapter.movieList.get(position).id);
            startActivity(intent);
        }
    };
}
