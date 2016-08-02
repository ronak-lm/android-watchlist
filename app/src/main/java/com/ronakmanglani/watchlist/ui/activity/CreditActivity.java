package com.ronakmanglani.watchlist.ui.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.WatchlistApp;
import com.ronakmanglani.watchlist.ui.fragment.CreditFragment;

import butterknife.BindBool;
import butterknife.ButterKnife;

public class CreditActivity extends AppCompatActivity {

    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            CreditFragment fragment = new CreditFragment();

            Bundle args = new Bundle();
            args.putInt(WatchlistApp.CREDIT_TYPE, getIntent().getIntExtra(WatchlistApp.CREDIT_TYPE, 0));
            args.putString(WatchlistApp.MOVIE_NAME, getIntent().getStringExtra(WatchlistApp.MOVIE_NAME));
            args.putParcelableArrayList(WatchlistApp.CREDIT_LIST, getIntent().getParcelableArrayListExtra(WatchlistApp.CREDIT_LIST));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.credit_container, fragment).commit();

            if (isTablet) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

}
