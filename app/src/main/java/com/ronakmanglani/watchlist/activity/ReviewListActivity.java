package com.ronakmanglani.watchlist.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.adapter.ReviewListAdapter;
import com.ronakmanglani.watchlist.adapter.ReviewListAdapter.OnReviewClickListener;
import com.ronakmanglani.watchlist.model.Review;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReviewListActivity extends AppCompatActivity implements OnReviewClickListener {

    // Key for intent extra
    public static final String MOVIE_ID_KEY = "movie_id";
    public static final String MOVIE_NAME_KEY = "movie_name";

    // Movie associated with the activity
    private String movieId;
    private String movieName;

    // Flag variables and counters
    private boolean isLoading = false;
    private boolean isLoadingLocked = false;
    private int pageToDownload = 1;
    private int totalPages = 1;

    // RecyclerView objects
    private ReviewListAdapter adapter;
    private LinearLayoutManager layoutManager;

    // Layout Views
    @Bind(R.id.toolbar)             Toolbar toolbar;
    @Bind(R.id.toolbar_title)       TextView toolbarTitle;
    @Bind(R.id.toolbar_subtitle)    TextView toolbarSubtitle;
    @Bind(R.id.review_list)         RecyclerView reviewList;
    @Bind(R.id.error_message)       View errorMessage;
    @Bind(R.id.no_results)          View noResults;
    @Bind(R.id.no_results_message)  TextView noResultsMessage;
    @Bind(R.id.progress_circle)     View progressCircle;
    @Bind(R.id.loading_more)        View loadingMore;

    // Activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        ButterKnife.bind(this);

        // Get intent extras
        movieId = getIntent().getStringExtra(MOVIE_ID_KEY);
        movieName = getIntent().getStringExtra(MOVIE_NAME_KEY);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbarTitle.setText(R.string.reviews_title);
        toolbarSubtitle.setText(movieName);

        // Setup RecyclerView
        adapter = new ReviewListAdapter(new ArrayList<Review>(), this);
        layoutManager = new LinearLayoutManager(this);
        reviewList.setHasFixedSize(true);
        reviewList.setLayoutManager(layoutManager);
        reviewList.setAdapter(adapter);
        reviewList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Load more data if reached the end of the list
                if (layoutManager.findLastVisibleItemPosition() == adapter.reviewList.size() - 1 && !isLoadingLocked && !isLoading) {
                    if (pageToDownload < totalPages) {
                        loadingMore.setVisibility(View.VISIBLE);
                        downloadMovieReviews();
                    }
                }
            }
        });

        // Download reviews
        if (savedInstanceState == null || !savedInstanceState.containsKey("review_list")) {
            downloadMovieReviews();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Cancel any pending network requests
        VolleySingleton.getInstance(this).requestQueue.cancelAll(this.getClass().getName());
    }

    // Save and restore layout state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (layoutManager != null && adapter != null) {
            outState.putParcelableArrayList("review_list", adapter.reviewList);
            outState.putBoolean("is_loading", isLoading);
            outState.putBoolean("is_locked", isLoadingLocked);
            outState.putInt("page_to_download", pageToDownload);
            outState.putInt("total_pages", totalPages);
        }
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.reviewList = savedInstanceState.getParcelableArrayList("review_list");
        totalPages = savedInstanceState.getInt("total_pages");
        pageToDownload = savedInstanceState.getInt("page_to_download");
        isLoadingLocked = savedInstanceState.getBoolean("is_locked");
        isLoading = savedInstanceState.getBoolean("is_loading");
        // If activity was previously downloading and it stopped, download again
        if (isLoading) {
            if (pageToDownload > 1) {
                progressCircle.setVisibility(View.GONE);
                reviewList.setVisibility(View.VISIBLE);
                loadingMore.setVisibility(View.VISIBLE);
            }
            downloadMovieReviews();
        } else {
            onDownloadSuccessful();
        }
    }

    // Toolbar actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return false;
        }
    }

    // Network related methods
    private void downloadMovieReviews() {
        // Initialize adapter if null
        if (adapter == null) {
            adapter = new ReviewListAdapter(new ArrayList<Review>(), this);
            reviewList.setAdapter(adapter);
        }
        // Set flag
        isLoading = true;
        // Download reviews
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, TMDBHelper.getMovieReviewsLink(this, movieId, pageToDownload), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject object) {
                        try {
                            // Parse reviews
                            JSONArray reviewsArray = object.getJSONArray("results");
                            for (int i = 0; i < reviewsArray.length(); i++) {
                                JSONObject review = reviewsArray.getJSONObject(i);
                                String id = review.getString("id");
                                String author = review.getString("author");
                                String body = review.getString("content");
                                String url = review.getString("url");
                                adapter.reviewList.add(new Review(id, author, body, url));
                            }
                            // Update counters
                            pageToDownload++;
                            totalPages = object.getInt("total_pages");
                            // Update UI
                            onDownloadSuccessful();
                        } catch (Exception ex) {
                            // Show error message on parsing errors
                            onDownloadFailed();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // Show error message on network errors
                        onDownloadFailed();
                    }
                });
        request.setTag(getClass().getName());
        VolleySingleton.getInstance(this).requestQueue.add(request);
    }
    private void onDownloadSuccessful() {
        isLoading = false;
        if (adapter.reviewList.size() == 0) {
            // Set text message for no reviews
            noResultsMessage.setText(R.string.reviews_no_results);
            // Toggle visibility
            noResults.setVisibility(View.VISIBLE);
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            reviewList.setVisibility(View.GONE);
        } else {
            // Toggle visibility
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            reviewList.setVisibility(View.VISIBLE);
            // Notify adapter of change
            adapter.notifyDataSetChanged();
        }
    }
    private void onDownloadFailed() {
        isLoading = false;
        if (pageToDownload == 1) {
            errorMessage.setVisibility(View.VISIBLE);
            reviewList.setVisibility(View.GONE);
        } else {
            errorMessage.setVisibility(View.GONE);
            reviewList.setVisibility(View.VISIBLE);
            isLoadingLocked = true;
        }
        progressCircle.setVisibility(View.GONE);
        loadingMore.setVisibility(View.GONE);
    }

    // Click events
    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        // Toggle visibility
        reviewList.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);
        // Reset counters
        pageToDownload = 1;
        totalPages = 1;
        // Download reviews again
        adapter = null;
        downloadMovieReviews();
    }
    @OnClick(R.id.write_review_button)
    public void onWriteReviewButtonClicked() {
        Intent writeReview = new Intent(Intent.ACTION_VIEW, Uri.parse(TMDBHelper.getWriteReviewLink(movieId)));
        startActivity(writeReview);
    }
    @Override
    public void onReviewClicked(int position) {
        Review review = adapter.reviewList.get(position);
        Intent intent = new Intent(this, ReviewDetailActivity.class);
        intent.putExtra(ReviewDetailActivity.REVIEW_KEY, review);
        intent.putExtra(ReviewDetailActivity.MOVIE_NAME_KEY, movieName);
        startActivity(intent);
    }
}
