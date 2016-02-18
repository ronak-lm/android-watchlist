package com.ronakmanglani.watchlist.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.fragment.ReviewDetailFragment;
import com.ronakmanglani.watchlist.fragment.ReviewListFragment;
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
            ReviewListFragment fragment = new ReviewListFragment();

            Bundle args = new Bundle();
            args.putString(Watchlist.MOVIE_ID, getIntent().getStringExtra(Watchlist.MOVIE_ID));
            args.putString(Watchlist.MOVIE_NAME, getIntent().getStringExtra(Watchlist.MOVIE_NAME));
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
        args.putString(Watchlist.MOVIE_NAME, movieName);
        args.putParcelable(Watchlist.REVIEW_OBJECT, review);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.review_detail_container, fragment).commit();
    }
}
