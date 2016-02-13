package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.fragment.MovieDetailFragment;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (savedInstanceState == null) {
            MovieDetailFragment fragment = new MovieDetailFragment();

            Bundle args = new Bundle();
            args.putString(Watchlist.MOVIE_ID, getIntent().getStringExtra(Watchlist.MOVIE_ID));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, fragment).commit();
        }
    }

}
