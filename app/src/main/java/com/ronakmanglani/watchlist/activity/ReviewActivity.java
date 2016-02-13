package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.fragment.MovieDetailFragment;
import com.ronakmanglani.watchlist.fragment.ReviewFragment;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        if (savedInstanceState == null) {
            ReviewFragment fragment = new ReviewFragment();

            Bundle args = new Bundle();
            args.putString(Watchlist.MOVIE_ID, getIntent().getStringExtra(Watchlist.MOVIE_ID));
            args.putString(Watchlist.MOVIE_NAME, getIntent().getStringExtra(Watchlist.MOVIE_NAME));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.review_container, fragment).commit();
        }
    }
}
