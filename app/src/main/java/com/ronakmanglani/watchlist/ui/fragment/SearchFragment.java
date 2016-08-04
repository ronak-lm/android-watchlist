package com.ronakmanglani.watchlist.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.WatchlistApp;
import com.ronakmanglani.watchlist.ui.activity.MovieDetailActivity;
import com.ronakmanglani.watchlist.ui.activity.SearchActivity;
import com.ronakmanglani.watchlist.ui.adapter.SearchAdapter;
import com.ronakmanglani.watchlist.model.Movie;
import com.ronakmanglani.watchlist.api.ApiHelper;
import com.ronakmanglani.watchlist.util.TextUtil;
import com.ronakmanglani.watchlist.api.VolleySingleton;
import com.ronakmanglani.watchlist.ui.view.PaddingDecorationView;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ronakmanglani.watchlist.ui.adapter.SearchAdapter.*;

public class SearchFragment extends Fragment implements OnMovieClickListener {

    private Context context;
    private Unbinder unbinder;

    private String searchQuery;
    private SearchAdapter adapter;
    private LinearLayoutManager layoutManager;

    private int pageToDownload;
    private int totalPages;
    private boolean isLoading;
    private boolean isLoadingLocked;
    @BindBool(R.bool.is_tablet) boolean isTablet;

    @BindView(R.id.toolbar)             Toolbar toolbar;
    @BindView(R.id.search_bar)          EditText searchBar;
    @BindView(R.id.error_message)       View errorMessage;
    @BindView(R.id.progress_circle)     View progressCircle;
    @BindView(R.id.loading_more)        View loadingMore;
    @BindView(R.id.no_results)          View noResults;
    @BindView(R.id.search_list)         RecyclerView recyclerView;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_list, container, false);
        context = getContext();
        unbinder = ButterKnife.bind(this, v);

        // Setup toolbar
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.action_home));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String query = searchBar.getText().toString().trim();
                    if (query.length() > 0) {
                        // Set query string
                        searchQuery = query;

                        // Toggle visibility
                        recyclerView.setVisibility(View.GONE);
                        errorMessage.setVisibility(View.GONE);
                        loadingMore.setVisibility(View.GONE);
                        noResults.setVisibility(View.GONE);
                        progressCircle.setVisibility(View.VISIBLE);

                        // Set counters
                        pageToDownload = 1;
                        totalPages = 1;

                        // Download list
                        adapter = null;
                        searchMoviesList();

                        return true;
                    }
                }
                return false;
            }
        });

        // Setup RecyclerView
        layoutManager = new GridLayoutManager(context, getNumberOfColumns());
        recyclerView.addItemDecoration(new PaddingDecorationView(context, R.dimen.recycler_item_padding));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Load more if RecyclerView has reached the end and isn't already loading
                if (layoutManager.findLastVisibleItemPosition() == adapter.movieList.size() - 1 && !isLoadingLocked && !isLoading) {
                    if (pageToDownload < totalPages) {
                        loadingMore.setVisibility(View.VISIBLE);
                        searchMoviesList();
                    }
                }
            }
        });

        // Get the movies list
        if (savedInstanceState != null && savedInstanceState.containsKey(WatchlistApp.MOVIE_LIST)) {
            adapter = new SearchAdapter(context, this);
            adapter.movieList = savedInstanceState.getParcelableArrayList(WatchlistApp.MOVIE_LIST);
            recyclerView.setAdapter(adapter);
            searchQuery = savedInstanceState.getString(WatchlistApp.SEARCH_QUERY);
            pageToDownload = savedInstanceState.getInt(WatchlistApp.PAGE_TO_DOWNLOAD);
            totalPages = savedInstanceState.getInt(WatchlistApp.TOTAL_PAGES);
            isLoadingLocked = savedInstanceState.getBoolean(WatchlistApp.IS_LOCKED);
            isLoading = savedInstanceState.getBoolean(WatchlistApp.IS_LOADING);
            // Download again if stopped, else show list
            if (isLoading) {
                if (pageToDownload == 1) {
                    progressCircle.setVisibility(View.VISIBLE);
                    loadingMore.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    progressCircle.setVisibility(View.GONE);
                    loadingMore.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                searchMoviesList();
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
            outState.putBoolean(WatchlistApp.IS_LOADING, isLoading);
            outState.putBoolean(WatchlistApp.IS_LOCKED, isLoadingLocked);
            outState.putInt(WatchlistApp.PAGE_TO_DOWNLOAD, pageToDownload);
            outState.putInt(WatchlistApp.TOTAL_PAGES, totalPages);
            outState.putString(WatchlistApp.SEARCH_QUERY, searchQuery);
            outState.putParcelableArrayList(WatchlistApp.MOVIE_LIST, adapter.movieList);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VolleySingleton.getInstance().requestQueue.cancelAll(getClass().getName());
        unbinder.unbind();
    }

    // Helper methods
    public int getNumberOfColumns() {
        // Get screen width
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPx = displayMetrics.widthPixels;
        if (isTablet) {
            widthPx = widthPx / 3;
        }
        float desiredPx = getResources().getDimensionPixelSize(R.dimen.movie_list_card_width);
        int columns = Math.round(widthPx / desiredPx);
        return columns > 1 ? columns : 1;
    }

    // JSON parsing and display
    private void searchMoviesList() {
        if (adapter == null) {
            adapter = new SearchAdapter(context, this);
            recyclerView.swapAdapter(adapter, true);
        }
        String urlToDownload = ApiHelper.getSearchMoviesLink(context, searchQuery, pageToDownload);
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
                                if (!TextUtil.isNullOrEmpty(year)) {
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
                                ((SearchActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(0).id);
                            }

                            // Update counters
                            pageToDownload = jsonObject.getInt("page") + 1;
                            totalPages = jsonObject.getInt("total_pages");

                            // Display search results
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
        VolleySingleton.getInstance().requestQueue.add(request);
    }
    private void onDownloadSuccessful() {
        isLoading = false;
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.GONE);
        loadingMore.setVisibility(View.GONE);
        if (adapter.movieList.size() == 0) {
            noResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
    private void onDownloadFailed() {
        isLoading = false;
        if (pageToDownload == 1) {
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorMessage.setVisibility(View.VISIBLE);
        } else {
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            errorMessage.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            isLoadingLocked = true;
        }
    }

    // Click events
    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        // Hide all views
        errorMessage.setVisibility(View.GONE);
        noResults.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        // Show progress circle
        progressCircle.setVisibility(View.VISIBLE);
        // Try to download the data again
        pageToDownload = 1;
        totalPages = 1;
        adapter = null;
        searchMoviesList();
    }
    @Override
    public void onMovieClicked(int position) {
        if (isTablet) {
            ((SearchActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(position).id);
        } else {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra(WatchlistApp.MOVIE_ID, adapter.movieList.get(position).id);
            startActivity(intent);
        }
    }
}
