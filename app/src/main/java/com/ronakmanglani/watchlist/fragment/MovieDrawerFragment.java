package com.ronakmanglani.watchlist.fragment;

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
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static android.support.design.widget.NavigationView.*;

public class MovieDrawerFragment extends Fragment implements OnMenuItemClickListener, OnNavigationItemSelectedListener {

    private MovieGridFragment fragment;
    private SharedPreferences preferences;

    // Layout Views
    @Bind(R.id.toolbar)         Toolbar toolbar;
    @Bind(R.id.drawer_layout)   DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view) NavigationView navigationView;

    // Fragment Initialization
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_drawer, container, false);
        ButterKnife.bind(this, v);
        preferences = getContext().getSharedPreferences(Watchlist.TABLE_USER, Context.MODE_PRIVATE);

        // Initialize options menu
        toolbar.inflateMenu(R.menu.menu_movie);
        toolbar.setOnMenuItemClickListener(this);
        initializeMenu();

        // Respond to clicks of NavigationView
        navigationView.setNavigationItemSelectedListener(this);

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
        int lastPosition = preferences.getInt(Watchlist.KEY_LAST_SELECTED, 0);
        if (savedInstanceState == null) {
            setSelectedDrawerItem(lastPosition);
        } else {
            if (savedInstanceState.containsKey("toolbar_title")) {
                toolbar.setTitle(savedInstanceState.getString("toolbar_title"));
            } else {
                toolbar.setTitle(navigationView.getMenu().getItem(lastPosition).getTitle());
            }
        }
        return v;
    }

    // Respond to clicks of toolbar menu
    private void initializeMenu() {
       if (preferences.getInt(Watchlist.KEY_VIEW_MODE, Watchlist.VIEW_MODE_GRID) == Watchlist.VIEW_MODE_GRID) {
            // Grid mode
            Menu menu = toolbar.getMenu();
            menu.findItem(R.id.action_grid).setVisible(false);
            menu.findItem(R.id.action_list).setVisible(true);
        } else {
            // List mode
            Menu menu = toolbar.getMenu();
            menu.findItem(R.id.action_grid).setVisible(true);
            menu.findItem(R.id.action_list).setVisible(false);
        }
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Toast.makeText(getActivity(), "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_list:
                SharedPreferences.Editor editor1 = preferences.edit();
                editor1.putInt(Watchlist.KEY_VIEW_MODE, Watchlist.VIEW_MODE_LIST);
                editor1.apply();
                initializeMenu();
                fragment.refreshLayout();
                return true;
            case R.id.action_grid:
                SharedPreferences.Editor editor2 = preferences.edit();
                editor2.putInt(Watchlist.KEY_VIEW_MODE, Watchlist.VIEW_MODE_GRID);
                editor2.apply();
                initializeMenu();
                fragment.refreshLayout();
                return true;
            default: return false;
        }
    }

    // Save fragment state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("toolbar_title", toolbar.getTitle().toString());
    }

    // Respond to selection of navigation drawer items
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
            default:
                return false;
        }
    }
    // Set "position" as current drawer item
    private void setSelectedDrawerItem(int position) {
        MenuItem item = navigationView.getMenu().getItem(position);
        // Set toolbar title
        toolbar.setTitle(item.getTitle());
        // Set selection in drawer
        item.setChecked(true);
        // Change the fragment
        fragment = new MovieGridFragment();
        Bundle args = new Bundle();
        if (position == 0) {
            args.putInt(MovieGridFragment.VIEW_TYPE_KEY, MovieGridFragment.VIEW_TYPE_POPULAR);
        } else if (position == 1) {
            args.putInt(MovieGridFragment.VIEW_TYPE_KEY, MovieGridFragment.VIEW_TYPE_RATED);
        } else if (position == 2) {
            args.putInt(MovieGridFragment.VIEW_TYPE_KEY, MovieGridFragment.VIEW_TYPE_UPCOMING);
        } else if (position == 3) {
            args.putInt(MovieGridFragment.VIEW_TYPE_KEY, MovieGridFragment.VIEW_TYPE_PLAYING);
        }
        fragment.setArguments(args);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
        // Save selected position to preference
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Watchlist.KEY_LAST_SELECTED, position);
        editor.apply();
    }
}
