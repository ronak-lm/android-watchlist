package com.ronakmanglani.watchlist.fragment;

import com.ronakmanglani.watchlist.util.TMDBHelper;

public class NowPlayingFragment extends BaseFragment {

    public String getUrlToDownload(int page) {
        return TMDBHelper.getNowPlayingMoviesLink(getActivity(), page);
    }

    public boolean isDetailedViewEnabled() {
        return false;
    }

    public int getSpanLocation() {
        return -1;
    }
}