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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.activity.ReviewActivity;
import com.ronakmanglani.watchlist.activity.ReviewDetailActivity;
import com.ronakmanglani.watchlist.adapter.ReviewAdapter;
import com.ronakmanglani.watchlist.model.Review;
import com.ronakmanglani.watchlist.util.ApiHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ReviewListFragment extends Fragment implements ReviewAdapter.OnReviewClickListener {

    private Tracker tracker;
    private Unbinder unbinder;

    private String movieId;
    private String movieName;

    private ReviewAdapter adapter;
    private LinearLayoutManager layoutManager;

    private int pageToDownload = 1;
    private int totalPages = 1;

    private boolean isLoading = false;
    private boolean isLoadingLocked = false;
    @BindBool(R.bool.is_tablet) boolean isTablet;

    @BindView(R.id.toolbar)             Toolbar toolbar;
    @BindView(R.id.toolbar_title)       TextView toolbarTitle;
    @BindView(R.id.toolbar_subtitle)    TextView toolbarSubtitle;
    @BindView(R.id.review_list)         RecyclerView reviewList;
    @BindView(R.id.error_message)       View errorMessage;
    @BindView(R.id.no_results)          View noResults;
    @BindView(R.id.no_results_message)  TextView noResultsMessage;
    @BindView(R.id.progress_circle)     View progressCircle;
    @BindView(R.id.loading_more)        View loadingMore;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_review,container,false);
        unbinder = ButterKnife.bind(this, v);

        // Initialize variables
        movieId = getArguments().getString(Watchlist.MOVIE_ID);
        movieName = getArguments().getString(Watchlist.MOVIE_NAME);
        tracker = ((Watchlist) getActivity().getApplication()).getTracker();

        // Setup toolbar
        toolbar.setTitle("");
        toolbarTitle.setText(R.string.reviews_title);
        toolbarSubtitle.setText(movieName);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.action_home));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

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
    public void onResume() {
        super.onResume();
        // Send screen name to analytics
        tracker.setScreenName(getString(R.string.screen_review_list));
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        VolleySingleton.getInstance(getContext()).requestQueue.cancelAll(this.getClass().getName());
        unbinder.unbind();
    }

    // JSON parsing and display
    private void downloadMovieReviews() {
        if (adapter == null) {
            adapter = new ReviewAdapter(new ArrayList<Review>(), this);
            reviewList.setAdapter(adapter);
        }
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, ApiHelper.getMovieReviewsLink(movieId, pageToDownload), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray array) {
                        try {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject review = array.getJSONObject(i);

                                String id = review.getString("id");
                                String comment = review.getString("comment");
                                boolean hasSpoiler = review.getBoolean("spoiler");

                                // Get date and format it
                                String inputTime = review.getString("created_at").substring(0, 10);
                                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = inputFormat.parse(inputTime);
                                DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
                                String createdAt = outputFormat.format(date);

                                // Get user name
                                JSONObject user = review.getJSONObject("user");
                                String userName = user.getString("username");
                                if (!user.getBoolean("private")) {
                                    String name = user.getString("name");
                                    if (name.length() > 0 && !name.equals("null")) {
                                        userName = name;
                                    }
                                }

                                adapter.reviewList.add(new Review(id, userName, comment, createdAt, hasSpoiler));
                            }

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
                        if (volleyError.networkResponse.statusCode == 404 || volleyError.networkResponse.statusCode == 405) {
                            // No such movie exists
                            onDownloadSuccessful();
                        } else {
                            // Network error, failed to load
                            onDownloadFailed();
                        }
                    }
                }) {
                    // Add Request Headers
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<>();
                        params.put("Content-type", "application/json");
                        params.put("trakt-api-key", ApiHelper.getTraktKey(getContext()));
                        params.put("trakt-api-version", "2");
                        return params;
                    }
                    // Get Response Headers
                    @Override
                    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                        pageToDownload = Integer.parseInt(response.headers.get("X-Pagination-Page")) + 1;
                        totalPages = Integer.parseInt(response.headers.get("X-Pagination-Page-Count"));
                        return super.parseNetworkResponse(response);
                    }
                };
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
            if (isTablet) {
                ((ReviewActivity) getActivity()).loadDetailFragmentWith("null", null);
            }
        } else {
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            loadingMore.setVisibility(View.GONE);
            reviewList.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            if (isTablet) {
                ((ReviewActivity) getActivity()).loadDetailFragmentWith(movieName, adapter.reviewList.get(0));
            }
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
        if (isTablet) {
            ((ReviewActivity) getActivity()).loadDetailFragmentWith(movieName, review);
        } else {
            Intent intent = new Intent(getContext(), ReviewDetailActivity.class);
            intent.putExtra(Watchlist.MOVIE_NAME, movieName);
            intent.putExtra(Watchlist.REVIEW_OBJECT, review);
            startActivity(intent);
        }
    }
}