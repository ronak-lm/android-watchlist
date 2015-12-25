package com.ronakmanglani.watchlist.fragment;

import com.ronakmanglani.watchlist.util.APIHelper;

public class MainPopularFragment extends MainBaseFragment {

    public String getUrlToDownload(int page) {
        return APIHelper.getMostPopularMoviesLink(getActivity(), page);
    }
}