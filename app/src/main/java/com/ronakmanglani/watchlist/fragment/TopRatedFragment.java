package com.ronakmanglani.watchlist.fragment;

import com.ronakmanglani.watchlist.util.TMDBHelper;

public class TopRatedFragment extends BaseMovieFragment {

    public String getUrlToDownload(int page) {
        return TMDBHelper.getHighestRatedMoviesLink(getActivity(), page);
    }

    public boolean isDetailedViewEnabled() {
        if (getNumberOfColumns() == 2) {
            return true;
        } else {
            return false;
        }
    }

    public int getSpanLocation() {
        return 1;
    }
}