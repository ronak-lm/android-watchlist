package com.ronakmanglani.watchlist.ui.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.WatchlistApp;
import com.ronakmanglani.watchlist.ui.fragment.ReviewDetailFragment;
import com.ronakmanglani.watchlist.ui.fragment.ReviewFragment;
import com.ronakmanglani.watchlist.model.Review;

import butterknife.BindBool;
import butterknife.ButterKnife;

public class ReviewActivity extends AppCompatActivity {

    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            ReviewFragment fragment = new ReviewFragment();

            Bundle args = new Bundle();
            args.putString(WatchlistApp.MOVIE_ID, getIntent().getStringExtra(WatchlistApp.MOVIE_ID));
            args.putString(WatchlistApp.MOVIE_NAME, getIntent().getStringExtra(WatchlistApp.MOVIE_NAME));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.review_container, fragment).commit();

            if (isTablet) {
                loadDetailFragmentWith("", null);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    public void loadDetailFragmentWith(String movieName, Review review) {
        ReviewDetailFragment fragment = new ReviewDetailFragment();
        Bundle args = new Bundle();
        args.putString(WatchlistApp.MOVIE_NAME, movieName);
        args.putParcelable(WatchlistApp.REVIEW_OBJECT, review);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.review_detail_container, fragment).commit();
    }
}
