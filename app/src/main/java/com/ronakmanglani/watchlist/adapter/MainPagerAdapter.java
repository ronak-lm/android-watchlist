package com.ronakmanglani.watchlist.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.fragment.MainPlayingFragment;
import com.ronakmanglani.watchlist.fragment.MainPopularFragment;
import com.ronakmanglani.watchlist.fragment.MainRatedFragment;
import com.ronakmanglani.watchlist.fragment.MainUpcomingFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private Context context;                // Context of calling activity
    public final int ITEM_COUNT = 4;        // Number of fragments

    // Constructor
    public MainPagerAdapter(Context context, FragmentManager manager) {
        super(manager);
        this.context = context;
    }

    // Adapter Methods
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new MainPopularFragment();
        } else if (position == 1) {
            return new MainRatedFragment();
        } else if (position == 2) {
            return new MainUpcomingFragment();
        } else {
            return new MainPlayingFragment();
        }
    }
    @Override
    public int getCount() {
        return ITEM_COUNT;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    // Custom Methods
    public int getIcon(int position) {
        if (position == 0) {
            return R.drawable.ic_tab_popular;
        } else if (position == 1) {
            return R.drawable.ic_tab_rated;
        } else if (position == 2) {
            return R.drawable.ic_tab_upcoming;
        } else {
            return R.drawable.ic_tab_playing;
        }
    }
    public String getTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.main_tab_popular);
        } else if (position == 1) {
            return context.getString(R.string.main_tab_rated);
        } else if (position == 2) {
            return context.getString(R.string.main_tab_upcoming);
        } else {
            return context.getString(R.string.main_tab_playing);
        }
    }
}
