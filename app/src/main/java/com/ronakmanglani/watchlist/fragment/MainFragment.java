package com.ronakmanglani.watchlist.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ronakmanglani.watchlist.R;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {

    // Key for SharedPreferences
    @BindString(R.string.settings_last_selection) String LAST_SELECTION_KEY;

    // Layout Views
    @Bind(R.id.toolbar)         Toolbar toolbar;
    @Bind(R.id.drawer_layout)   DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view) NavigationView navigationView;

    // Fragment Initialization
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, v);

        // Initialize options menu
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_search:
                        Toast.makeText(getActivity(), "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_about:
                        Toast.makeText(getActivity(), "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                        return true;
                    default: return false;
                }
            }
        });

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
                        setSelectedDrawerItem(0);
                        return true;
                    case R.id.drawer_rated:
                        setSelectedDrawerItem(1);
                        return true;
                    case R.id.drawer_upcoming:
                        setSelectedDrawerItem(2);
                        return true;
                    case R.id.drawer_playing:
                        setSelectedDrawerItem(3);
                        return true;
                    case R.id.drawer_favorite:
                        setSelectedDrawerItem(4);
                        return true;
                    case R.id.drawer_watchlist:
                        setSelectedDrawerItem(5);
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Respond to opening/closing of navigation drawer
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
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
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        int lastPosition = preferences.getInt(LAST_SELECTION_KEY, 0);
        setSelectedDrawerItem(lastPosition);
        return v;
    }

    // Set "position" as current drawer item
    private void setSelectedDrawerItem(int position) {
        MenuItem item = navigationView.getMenu().getItem(position);
        // Set toolbar title
        toolbar.setTitle(item.getTitle());
        // Set selection in drawer
        item.setChecked(true);
        // Change the fragment
        Fragment fragment;
        if (position == 0) {
            fragment = new MostPopularFragment();
        } else if (position == 1) {
            fragment = new TopRatedFragment();
        } else if (position == 2) {
            fragment = new UpcomingFragment();
        } else if (position == 3) {
            fragment = new NowPlayingFragment();
        } else if (position == 4) {
            fragment = new FavoritesFragment();
        } else {
            fragment = new WatchlistFragment();
        }
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
        // Save selected position to preference
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LAST_SELECTION_KEY, position);
        editor.apply();
    }

}
