package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.fragment.DetailFragment;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (isTablet) {
            loadDetailFragmentWith("");
        }
    }

    public void loadDetailFragmentWith(String movieId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(DetailActivity.MOVIE_ID, movieId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment, fragment).commit();
    }
}
