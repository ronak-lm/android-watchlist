package com.ronakmanglani.watchlist.fragment;

import com.ronakmanglani.watchlist.util.APIHelper;

public class MainPlayingFragment extends MainBaseFragment {

    public String getUrlToDownload(int page) {
        return APIHelper.getNowPlayingMoviesLink (getActivity(), page);
    }

    public boolean isDetailedViewEnabled() {
        return false;
    }
}