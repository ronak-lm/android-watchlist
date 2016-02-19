package com.ronakmanglani.watchlist.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.adapter.PhotoAdapter;
import com.ronakmanglani.watchlist.adapter.PhotoAdapter.OnPhotoClickListener;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.widget.ItemPaddingDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoActivity extends AppCompatActivity implements OnPhotoClickListener {

    private String movieId;
    private PhotoAdapter adapter;

    private boolean isLoading = false;
    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Bind(R.id.toolbar)             Toolbar toolbar;
    @Bind(R.id.toolbar_title)       TextView toolbarTitle;
    @Bind(R.id.toolbar_subtitle)    TextView toolbarSubtitle;
    @Bind(R.id.photo_list)          RecyclerView photoList;
    @Bind(R.id.error_message)       View errorMessage;
    @Bind(R.id.progress_circle)     View progressCircle;
    @Bind(R.id.no_results)          View noResults;
    @Bind(R.id.no_results_message)  TextView noResultsMessage;

    // Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);

        movieId = getIntent().getStringExtra(Watchlist.MOVIE_ID);
        String movieName = getIntent().getStringExtra(Watchlist.MOVIE_NAME);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbarTitle.setText(R.string.photos_title);
        toolbarSubtitle.setText(movieName);

        GridLayoutManager layoutManager = new GridLayoutManager(this,getNumberOfColumns());
        adapter = new PhotoAdapter(this, new ArrayList<String>(), this);
        photoList.setHasFixedSize(true);
        photoList.setLayoutManager(layoutManager);
        photoList.addItemDecoration(new ItemPaddingDecoration(this, R.dimen.dist_xxsmall));
        photoList.setAdapter(adapter);

        if (savedInstanceState == null) {
            downloadPhotosList();
        }

        // Lock orientation for tablets
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
            outState.putStringArrayList(Watchlist.PHOTO_LIST, adapter.photoList);
            outState.putBoolean(Watchlist.IS_LOADING, isLoading);
        }
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.photoList = savedInstanceState.getStringArrayList(Watchlist.PHOTO_LIST);
        isLoading = savedInstanceState.getBoolean(Watchlist.IS_LOADING);
        // If activity was previously downloading and it stopped, download again
        if (isLoading) {
            downloadPhotosList();
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

    // Helper method
    public int getNumberOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPx = displayMetrics.widthPixels;
        float desiredPx = getResources().getDimensionPixelSize(R.dimen.photo_item_width);
        int columns = Math.round(widthPx / desiredPx);
        if (columns <= 1) {
            return 1;
        } else {
            return columns;
        }
    }

    // JSON parsing and display
    private void downloadPhotosList() {
        isLoading = true;
        if (adapter == null) {
            adapter = new PhotoAdapter(this, new ArrayList<String>(), this);
            photoList.setAdapter(adapter);
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, TMDBHelper.getPhotosLink(this, movieId), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject object) {
                        try {
                            JSONArray backdrops = object.getJSONArray("backdrops");
                            for (int i = 0; i < backdrops.length(); i++) {
                                adapter.photoList.add(backdrops.getJSONObject(i).getString("file_path"));
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
        if (adapter.photoList.size() == 0) {
            noResultsMessage.setText(R.string.photos_no_results);
            noResults.setVisibility(View.VISIBLE);
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            photoList.setVisibility(View.GONE);
        } else {
            errorMessage.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            photoList.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
    private void onDownloadFailed() {
        isLoading = false;
        errorMessage.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.GONE);
        photoList.setVisibility(View.GONE);
    }

    // Click events
    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        photoList.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);
        adapter = null;
        downloadPhotosList();
    }
    @Override
    public void onPhotoClicked(int position) {
        String url = TMDBHelper.getOriginalImageURL(adapter.photoList.get(position));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
