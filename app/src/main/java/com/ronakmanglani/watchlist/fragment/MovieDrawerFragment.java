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
import android.view.ViewGroup;
import android.widget.Toast;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.design.widget.NavigationView.*;

public class MovieDrawerFragment extends Fragment implements OnMenuItemClickListener, OnNavigationItemSelectedListener {

    private MovieGridFragment fragment;
    private SharedPreferences preferences;

    @Bind(R.id.toolbar)         Toolbar toolbar;
    @Bind(R.id.drawer_layout)   DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view) NavigationView navigationView;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_drawer, container, false);
        ButterKnife.bind(this, v);
        preferences = getContext().getSharedPreferences(Watchlist.TABLE_USER, Context.MODE_PRIVATE);

        // Setup toolbar
        toolbar.inflateMenu(R.menu.menu_movie);
        toolbar.setOnMenuItemClickListener(this);
        onRefreshToolbarMenu();

        // Setup navigation drawer
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
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle.syncState();

        // Load previously selected drawer item
        int lastPosition = preferences.getInt(Watchlist.LAST_SELECTED, 0);
        if (savedInstanceState == null) {
            setSelectedDrawerItem(lastPosition);
        } else {
            fragment = (MovieGridFragment) getActivity().getSupportFragmentManager().findFragmentByTag(Watchlist.TAG_GRID_FRAGMENT);
            if (savedInstanceState.containsKey(Watchlist.TOOLBAR_TITLE)) {
                toolbar.setTitle(savedInstanceState.getString(Watchlist.TOOLBAR_TITLE));
            } else {
                toolbar.setTitle(navigationView.getMenu().getItem(lastPosition).getTitle());
            }
        }
        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Watchlist.TOOLBAR_TITLE, toolbar.getTitle().toString());
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    // Toolbar action menu
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Toast.makeText(getActivity(), "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_list:
                SharedPreferences.Editor editor1 = preferences.edit();
                editor1.putInt(Watchlist.VIEW_MODE, Watchlist.VIEW_MODE_LIST);
                editor1.apply();
                onRefreshToolbarMenu();
                fragment.refreshLayout();
                return true;
            case R.id.action_grid:
                SharedPreferences.Editor editor2 = preferences.edit();
                editor2.putInt(Watchlist.VIEW_MODE, Watchlist.VIEW_MODE_GRID);
                editor2.apply();
                onRefreshToolbarMenu();
                fragment.refreshLayout();
                return true;
            default: return false;
        }
    }
    private void onRefreshToolbarMenu() {
        if (preferences.getInt(Watchlist.VIEW_MODE, Watchlist.VIEW_MODE_GRID) == Watchlist.VIEW_MODE_GRID) {
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

    // Drawer item selection
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawers();
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
    private void setSelectedDrawerItem(int position) {
        MenuItem item = navigationView.getMenu().getItem(position);
        item.setChecked(true);
        toolbar.setTitle(item.getTitle());
        // Create and replace fragment
        fragment = new MovieGridFragment();
        Bundle args = new Bundle();
        if (position == 0) {
            args.putInt(Watchlist.VIEW_TYPE, Watchlist.VIEW_TYPE_POPULAR);
        } else if (position == 1) {
            args.putInt(Watchlist.VIEW_TYPE, Watchlist.VIEW_TYPE_RATED);
        } else if (position == 2) {
            args.putInt(Watchlist.VIEW_TYPE, Watchlist.VIEW_TYPE_UPCOMING);
        } else if (position == 3) {
            args.putInt(Watchlist.VIEW_TYPE, Watchlist.VIEW_TYPE_PLAYING);
        }
        fragment.setArguments(args);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment, Watchlist.TAG_GRID_FRAGMENT);
        transaction.commit();
        // Save selected position to preference
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Watchlist.LAST_SELECTED, position);
        editor.apply();
    }
}
