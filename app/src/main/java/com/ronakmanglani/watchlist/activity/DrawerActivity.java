package com.ronakmanglani.watchlist.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.fragment.MainPlayingFragment;
import com.ronakmanglani.watchlist.fragment.MainPopularFragment;
import com.ronakmanglani.watchlist.fragment.MainRatedFragment;
import com.ronakmanglani.watchlist.fragment.MainUpcomingFragment;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class DrawerActivity extends AppCompatActivity {

    // Key for SharedPreferences
    @BindString(R.string.settings_last_page) String LAST_SELECTION_KEY;

    // Layout Views
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view) NavigationView navigationView;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Respond to clicks of NavigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Close the drawer
                drawerLayout.closeDrawers();
                // Load the fragment required
                int id = item.getItemId();
                switch (id) {
                    case R.id.drawer_popular:
                        selectDrawerItem(0);
                        return true;
                    case R.id.drawer_rated:
                        selectDrawerItem(1);
                        return true;
                    case R.id.drawer_upcoming:
                        selectDrawerItem(2);
                        return true;
                    case R.id.drawer_playing:
                        selectDrawerItem(3);
                        return true;
                    case R.id.drawer_favorite:
                        return true;
                    case R.id.drawer_watchlist:
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Respond to opening/closing of navigation drawer
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the ActionBarToggle to DrawerLayout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        // Add hamburger icon to Toolbar
        actionBarDrawerToggle.syncState();
        // Load the last selected item from drawer
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        int lastPosition = preferences.getInt(LAST_SELECTION_KEY, 0);
        selectDrawerItem(lastPosition);
    }

    private void selectDrawerItem(int position) {
        MenuItem item = navigationView.getMenu().getItem(position);
        // Set toolbar title
        setTitle(item.getTitle());
        // Set selection in drawer
        item.setChecked(true);
        // Change the fragment
        Fragment fragment;
        if (position == 0) {
            fragment = new MainPopularFragment();
        } else if (position == 1) {
            fragment = new MainRatedFragment();
        } else if (position == 2) {
            fragment = new MainUpcomingFragment();
        } else {
            fragment = new MainPlayingFragment();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
        // Save selected position to preference
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LAST_SELECTION_KEY, position);
        editor.apply();
    }
}
