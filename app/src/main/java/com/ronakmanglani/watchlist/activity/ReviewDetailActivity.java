package com.ronakmanglani.watchlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Review;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.YoutubeHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReviewDetailActivity extends AppCompatActivity {

    // Key for intent extra
    public static final String REVIEW_KEY = "movie_review";
    public static final String MOVIE_NAME_KEY = "movie_name";

    // Review associated with the activity
    private String movieName;
    private Review review;

    // Layout Views
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.review_body) TextView reviewBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);
        ButterKnife.bind(this);

        // Get intent extra
        movieName = getIntent().getStringExtra(MOVIE_NAME_KEY);
        review = getIntent().getParcelableExtra(REVIEW_KEY);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Review by " + review.author);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set review body
        reviewBody.setText(review.body);
    }

    // Toolbar options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_share) {
            String shareText = "A review of " + movieName + " by " + review.author + " - " + review.url;
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, movieName + " - Review");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share_using)));
            return true;
        } else {
            return false;
        }
    }
}
