package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.fragment.ReviewDetailFragment;

public class ReviewDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        if (savedInstanceState == null) {
            ReviewDetailFragment fragment = new ReviewDetailFragment();

            Bundle args = new Bundle();
            args.putString(Watchlist.MOVIE_NAME, getIntent().getStringExtra(Watchlist.MOVIE_NAME));
            args.putParcelable(Watchlist.REVIEW_OBJECT, getIntent().getParcelableExtra(Watchlist.REVIEW_OBJECT));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }
}
