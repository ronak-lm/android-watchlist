package com.ronakmanglani.watchlist.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.adapter.VideoAdapter;
import com.ronakmanglani.watchlist.adapter.VideoAdapter.OnVideoClickListener;
import com.ronakmanglani.watchlist.model.Video;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.util.YoutubeHelper;
import com.ronakmanglani.watchlist.widget.ItemPaddingDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoActivity extends AppCompatActivity implements OnVideoClickListener {

    // Key for intent extra
    public static final String MOVIE_ID_KEY = "movie_id";
    public static final String MOVIE_NAME_KEY = "movie_name";

    // Movie associated with the activity
    private String movieId;

    // Flag
    private boolean isLoading = false;
    // Adapter for RecyclerView
    private VideoAdapter adapter;

    // Layout Views
    @Bind(R.id.toolbar)             Toolbar toolbar;
    @Bind(R.id.toolbar_title)       TextView toolbarTitle;
    @Bind(R.id.toolbar_subtitle)    TextView toolbarSubtitle;
    @Bind(R.id.video_list)          RecyclerView videoList;
    @Bind(R.id.error_message)       View errorMessage;
    @Bind(R.id.progress_circle)     View progressCircle;
    @Bind(R.id.no_results)          View noResults;
    @Bind(R.id.no_results_message)  TextView noResultsMessage;

    // Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);

        // Get intent extras
        movieId = getIntent().getStringExtra(MOVIE_ID_KEY);
        String movieName = getIntent().getStringExtra(MOVIE_NAME_KEY);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbarTitle.setText(R.string.videos_title);
        toolbarSubtitle.setText(movieName);

        // Setup RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this,getNumberOfColumns());
        adapter = new VideoAdapter(this, new ArrayList<Video>(), this);
        videoList.setHasFixedSize(true);
        videoList.setLayoutManager(layoutManager);
        videoList.addItemDecoration(new ItemPaddingDecoration(this, R.dimen.video_item_padding));
        videoList.setAdapter(adapter);

        // Download videos
        if (savedInstanceState == null) {
            downloadVideosList();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        VolleySingleton.getInstance(this).requestQueue.cancelAll(this.getClass().getName());
    }

    // Save/restore state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (adapter != null) {
            outState.putParcelableArrayList("videos_list", adapter.videoList);
            outState.putBoolean("is_loading", isLoading);
        }
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.videoList = savedInstanceState.getParcelableArrayList("videos_list");
        isLoading = savedInstanceState.getBoolean("is_loading");
        // If activity was previously downloading and it stopped, download again
        if (isLoading) {
            downloadVideosList();
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

    // Network and parsing methods
    private void downloadVideosList() {
        isLoading = true;
        if (adapter == null) {
            adapter = new VideoAdapter(this, new ArrayList<Video>(), this);
            videoList.setAdapter(adapter);
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, TMDBHelper.getVideosLink(this, movieId), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject object) {
                        try {
                            JSONArray results = object.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject vid = results.getJSONObject(i);
                                if (vid.getString("site").equals("YouTube")) {
                                    String title = vid.getString("name");
                                    String key = vid.getString("key");
                                    String subtitle = vid.getString("size") + "p";
                                    Video video = new Video(title, subtitle, key, YoutubeHelper.getThumbnailURL(key), YoutubeHelper.getVideoURL(key));
                                    adapter.videoList.add(video);
                                }
                            }
                            onDownloadSuccessful();
                        } catch (Exception ex) {
                            onDownloadFailed();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onDownloadFailed();
                    }
                });
        request.setTag(getClass().getName());
        VolleySingleton.getInstance(this).requestQueue.add(request);
    }
    private void onDownloadSuccessful() {
        isLoading = false;
        if (adapter.videoList.size() == 0) {
            noResultsMessage.setText(R.string.videos_no_results);
            noResults.setVisibility(View.VISIBLE);
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            videoList.setVisibility(View.GONE);
        } else {
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            videoList.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
    private void onDownloadFailed() {
        isLoading = false;
        errorMessage.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.GONE);
        videoList.setVisibility(View.GONE);
    }

    // Helper methods
    public int getNumberOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPx = displayMetrics.widthPixels;
        float desiredPx = getResources().getDimensionPixelSize(R.dimen.video_item_width);
        int columns = Math.round(widthPx / desiredPx);
        if (columns <= 1) {
            return 1;
        } else {
            return columns;
        }
    }

    // Click events
    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        videoList.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);
        adapter = null;
        downloadVideosList();
    }
    @Override
    public void onVideoClicked(int position) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + adapter.videoList.get(position).youtubeID));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="  + adapter.videoList.get(position).youtubeID));
            startActivity(intent);
        }
    }
}
