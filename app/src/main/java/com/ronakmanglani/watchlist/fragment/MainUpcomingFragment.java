package com.ronakmanglani.watchlist.fragment;

import com.ronakmanglani.watchlist.util.APIHelper;

public class MainUpcomingFragment extends MainBaseFragment {

    public String getUrlToDownload(int page) {
        return APIHelper.getUpcomingMoviesLink (getActivity(), page);
    }

    public boolean isDetailedViewEnabled() {
        return false;
    }
}