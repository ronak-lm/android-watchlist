package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ronakmanglani.watchlist.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DrawerActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view) NavigationView navigationView;

    // Start activity
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
                if (!item.isChecked()) {
                    item.setChecked(true);
                }

                drawerLayout.closeDrawers();

                int id = item.getItemId();
                switch (id) {
                    case R.id.drawer_popular:
                        return true;
                    case R.id.drawer_rated:
                        return true;
                    case R.id.drawer_upcoming:
                        return true;
                    case R.id.drawer_playing:
                        return true;
                    case R.id.drawer_favorite:
                        return true;
                    case R.id.drawer_watchlist:
                        return true;
                    default: return false;
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
    }
}
