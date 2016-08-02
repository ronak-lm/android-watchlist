package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.WatchlistApp;
import com.ronakmanglani.watchlist.fragment.ReviewDetailFragment;

public class ReviewDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        if (savedInstanceState == null) {
            ReviewDetailFragment fragment = new ReviewDetailFragment();

            Bundle args = new Bundle();
            args.putString(WatchlistApp.MOVIE_NAME, getIntent().getStringExtra(WatchlistApp.MOVIE_NAME));
            args.putParcelable(WatchlistApp.REVIEW_OBJECT, getIntent().getParcelableExtra(WatchlistApp.REVIEW_OBJECT));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.review_detail_container, fragment).commit();
        }
    }
}
