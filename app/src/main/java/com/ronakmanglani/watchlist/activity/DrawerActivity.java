package com.ronakmanglani.watchlist.activity;

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
import android.widget.Button;

import com.ronakmanglani.watchlist.R;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class DrawerActivity extends AppCompatActivity {

    // Key for SharedPreferences
    @BindString(R.string.settings_last_page) String LAST_SELECTION;

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
                // Set selected item as checked
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                // Close the drawer
                drawerLayout.closeDrawers();
                // Load the fragment required
                int id = item.getItemId();
                switch (id) {
                    case R.id.drawer_popular:
                        saveSelection(0);
                        return true;
                    case R.id.drawer_rated:
                        saveSelection(1);
                        return true;
                    case R.id.drawer_upcoming:
                        saveSelection(2);
                        return true;
                    case R.id.drawer_playing:
                        saveSelection(3);
                        return true;
                    case R.id.drawer_favorite:
                        saveSelection(4);
                        return true;
                    case R.id.drawer_watchlist:
                        saveSelection(5);
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
        // Load the last selected item from drawer
        loadSelection();
    }

    private void loadSelection() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        int lastPosition = preferences.getInt(LAST_SELECTION, 0);
        navigationView.getMenu().getItem(lastPosition).setChecked(true);
    }
    private void saveSelection(int position) {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LAST_SELECTION, position);
        editor.apply();
    }
}
