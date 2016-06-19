package com.ronakmanglani.watchlist.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;

public class BackupFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Watchlist.TABLE_USER);
        addPreferencesFromResource(R.xml.pref_backup);
    }

}