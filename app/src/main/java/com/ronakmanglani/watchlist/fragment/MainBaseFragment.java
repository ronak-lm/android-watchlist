package com.ronakmanglani.watchlist.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

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

    private Context context;                // Activity context
    private int pageToDownload = 1;         // Page number to download
    private int totalPages = 999;           // Total pages that can be downloaded
    private boolean isLoading;              // Flag for loading

    // Views for reference
    private View errorMessage;
    private View progressCircle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MainRecyclerAdapter adapter;
    private LinearLayoutManager layoutManager;

    // Abstract method
    public abstract String getUrlToDownload(int page);

    // Fragment Initialization
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main,container,false);
        context = getContext();

        // Find and initialize views
        errorMessage = v.findViewById(R.id.error_message);
        progressCircle = v.findViewById(R.id.progress_circle);
        layoutManager = new GridLayoutManager(context, getNumberOfColumns());
        adapter = new MainRecyclerAdapter(context, onClickListener);
        recyclerView = (RecyclerView) v.findViewById(R.id.movie_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

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
                    if (pageToDownload < totalPages) {
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

        // Download data from TMDB
        if (savedInstanceState == null) {
            downloadMoviesList();
        } else {
            // Restore data
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
        }

        return v;
    }
    // Returns the number of columns to display in the RecyclerView
    private int getNumberOfColumns() {
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
        outState.putInt("pageToDownload", pageToDownload);
        outState.putParcelable("layoutManagerState", layoutManager.onSaveInstanceState());
        outState.putParcelableArrayList("movieList", adapter.movieList);
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
            adapter = new MainRecyclerAdapter(context, onClickListener);
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
                                String id = movie.getString("id");
                                String name = movie.getString("title");
                                String year = movie.getString("release_date");
                                String rating = movie.getString("vote_average");
                                if (year.length() != 0) {
                                    year = year.substring(0, 4);
                                }
                                String imageURL = movie.getString("backdrop_path");
                                if (imageURL == null || imageURL.equals("null")) {
                                    imageURL = movie.getString("poster_path");
                                }
                                // Create MovieThumb object and add to list
                                Movie thumb = new Movie(id, name, year, rating, imageURL);
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
        // Open Movie
        @Override
        public void onCardClicked(int position) {
            Intent intent = new Intent(context, MovieActivity.class);
            intent.putExtra(MovieActivity.MOVIE_ID, adapter.movieList.get(position).id);
            startActivity(intent);
        }
        // Open Menu
        @Override
        public void onMenuClicked(final int position, ImageButton menuButton) {
            PopupMenu popupMenu = new PopupMenu(context, menuButton);
            popupMenu.inflate(R.menu.menu_movie_popup);
            /*
            TODO: Change text of menu items dynamically
            EXAMPLE: popupMenu.getMenu().getItem(0).setTitle("a");
            */
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.menu_myMovies:
                            //TODO: Logic to add/remove to/from my movies
                            Toast.makeText(context, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                            return true;

                        case R.id.menu_watchlist:
                            //TODO: Logic to add/remove to/from watchlist
                            Toast.makeText(context, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                            return true;

                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }
    };
}
