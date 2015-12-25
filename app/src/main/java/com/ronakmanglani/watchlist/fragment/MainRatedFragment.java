package com.ronakmanglani.watchlist.fragment;

import com.ronakmanglani.watchlist.util.APIHelper;

public class MainRatedFragment extends MainBaseFragment {

    public String getUrlToDownload(int page) {
        return APIHelper.getHighestRatedMoviesLink(getActivity(), page);
    }
}