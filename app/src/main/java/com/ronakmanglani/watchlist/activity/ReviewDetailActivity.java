package com.ronakmanglani.watchlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.fragment.ReviewDetailFragment;
import com.ronakmanglani.watchlist.fragment.ReviewFragment;
import com.ronakmanglani.watchlist.model.Review;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReviewDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        if (savedInstanceState == null) {
            ReviewDetailFragment fragment = new ReviewDetailFragment();

            Bundle args = new Bundle();
            args.putString(Watchlist.MOVIE_NAME, getIntent().getStringExtra(Watchlist.MOVIE_NAME));
            args.putParcelable(Watchlist.REVIEW_OBJECT, getIntent().getParcelableExtra(Watchlist.REVIEW_OBJECT));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.review_detail_container, fragment).commit();
        }
    }
}
