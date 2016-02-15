package com.ronakmanglani.watchlist.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.activity.ReviewDetailActivity;
import com.ronakmanglani.watchlist.adapter.ReviewAdapter;
import com.ronakmanglani.watchlist.model.Review;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReviewFragment extends Fragment implements ReviewAdapter.OnReviewClickListener {

    private String movieId;
    private String movieName;

    private ReviewAdapter adapter;
    private LinearLayoutManager layoutManager;

    private int pageToDownload = 1;
    private int totalPages = 1;

    private boolean isLoading = false;
    private boolean isLoadingLocked = false;
    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Bind(R.id.toolbar)             Toolbar toolbar;
    @Bind(R.id.toolbar_title)       TextView toolbarTitle;
    @Bind(R.id.toolbar_subtitle)    TextView toolbarSubtitle;
    @Bind(R.id.review_list)         RecyclerView reviewList;
    @Bind(R.id.error_message)       View errorMessage;
    @Bind(R.id.no_results)          View noResults;
    @Bind(R.id.no_results_message)  TextView noResultsMessage;
    @Bind(R.id.progress_circle)     View progressCircle;
    @Bind(R.id.loading_more)        View loadingMore;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_review,container,false);
        ButterKnife.bind(this, v);

        // Get arguments
        movieId = getArguments().getString(Watchlist.MOVIE_ID);
        movieName = getArguments().getString(Watchlist.MOVIE_NAME);

        // Setup toolbar
        toolbar.setTitle("");
        toolbarTitle.setText(R.string.reviews_title);
        toolbarSubtitle.setText(movieName);
        if (!isTablet) {
            toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.action_home));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });
        }

        // Setup RecyclerView
        adapter = new ReviewAdapter(new ArrayList<Review>(), this);
        layoutManager = new LinearLayoutManager(getContext());
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
        if (savedInstanceState == null || !savedInstanceState.containsKey(Watchlist.REVIEW_LIST)) {
            downloadMovieReviews();
        } else {
            adapter.reviewList = savedInstanceState.getParcelableArrayList(Watchlist.REVIEW_LIST);
            totalPages = savedInstanceState.getInt(Watchlist.TOTAL_PAGES);
            pageToDownload = savedInstanceState.getInt(Watchlist.PAGE_TO_DOWNLOAD);
            isLoadingLocked = savedInstanceState.getBoolean(Watchlist.IS_LOCKED);
            isLoading = savedInstanceState.getBoolean(Watchlist.IS_LOADING);
            // If download stopped, download again, else display list
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

        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (layoutManager != null && adapter != null) {
            outState.putParcelableArrayList(Watchlist.REVIEW_LIST, adapter.reviewList);
            outState.putBoolean(Watchlist.IS_LOADING, isLoading);
            outState.putBoolean(Watchlist.IS_LOCKED, isLoadingLocked);
            outState.putInt(Watchlist.PAGE_TO_DOWNLOAD, pageToDownload);
            outState.putInt(Watchlist.TOTAL_PAGES, totalPages);
        }
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        VolleySingleton.getInstance(getContext()).requestQueue.cancelAll(this.getClass().getName());
    }

    // JSON parsing and display
    private void downloadMovieReviews() {
        if (adapter == null) {
            adapter = new ReviewAdapter(new ArrayList<Review>(), this);
            reviewList.setAdapter(adapter);
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, TMDBHelper.getMovieReviewsLink(getContext(), movieId, pageToDownload), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject object) {
                        try {
                            JSONArray reviewsArray = object.getJSONArray("results");
                            for (int i = 0; i < reviewsArray.length(); i++) {
                                JSONObject review = reviewsArray.getJSONObject(i);
                                String id = review.getString("id");
                                String author = review.getString("author");
                                String body = review.getString("content");
                                String url = review.getString("url");
                                adapter.reviewList.add(new Review(id, author, body, url));
                            }

                            pageToDownload++;
                            totalPages = object.getInt("total_pages");

                            onDownloadSuccessful();

                        } catch (Exception ex) {
                            // Parsing error
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
        request.setTag(getClass().getName());
        VolleySingleton.getInstance(getContext()).requestQueue.add(request);
    }
    private void onDownloadSuccessful() {
        isLoading = false;
        if (adapter.reviewList.size() == 0) {
            noResultsMessage.setText(R.string.reviews_no_results);
            noResults.setVisibility(View.VISIBLE);
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            reviewList.setVisibility(View.GONE);
        } else {
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            reviewList.setVisibility(View.VISIBLE);
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
    @Override
    public void onReviewClicked(int position) {
        Review review = adapter.reviewList.get(position);
        Intent intent = new Intent(getContext(), ReviewDetailActivity.class);
        intent.putExtra(Watchlist.MOVIE_NAME, movieName);
        intent.putExtra(Watchlist.REVIEW_OBJECT, review);
        startActivity(intent);
    }
}