package com.ronakmanglani.watchlist.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.fragment.FavoritesFragment;
import com.ronakmanglani.watchlist.fragment.MostPopularFragment;
import com.ronakmanglani.watchlist.fragment.NowPlayingFragment;
import com.ronakmanglani.watchlist.fragment.TopRatedFragment;
import com.ronakmanglani.watchlist.fragment.UpcomingFragment;
import com.ronakmanglani.watchlist.fragment.WatchlistFragment;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
