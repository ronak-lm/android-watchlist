package com.ronakmanglani.watchlist.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.WatchlistApp;
import com.ronakmanglani.watchlist.fragment.MovieDetailFragment;

import butterknife.BindBool;
import butterknife.ButterKnife;

public class MovieActivity extends AppCompatActivity {

    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        ButterKnife.bind(this);
        if (isTablet && savedInstanceState == null) {
            loadDetailFragmentWith("null");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public void loadDetailFragmentWith(String movieId) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putString(WatchlistApp.MOVIE_ID, movieId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment, fragment).commit();
    }
}
