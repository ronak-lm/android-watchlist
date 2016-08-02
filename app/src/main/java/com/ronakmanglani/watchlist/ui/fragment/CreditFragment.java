package com.ronakmanglani.watchlist.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.WatchlistApp;
import com.ronakmanglani.watchlist.ui.adapter.CreditAdapter;
import com.ronakmanglani.watchlist.ui.adapter.CreditAdapter.OnCreditClickListener;
import com.ronakmanglani.watchlist.model.Credit;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CreditFragment extends Fragment implements OnCreditClickListener {

    private Unbinder unbinder;

    @BindView(R.id.toolbar)             Toolbar toolbar;
    @BindView(R.id.toolbar_title)       TextView toolbarTitle;
    @BindView(R.id.toolbar_subtitle)    TextView toolbarSubtitle;
    @BindView(R.id.credit_list)         RecyclerView creditView;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_credit, container, false);
        unbinder = ButterKnife.bind(this, v);

        int creditType = getArguments().getInt(WatchlistApp.CREDIT_TYPE);
        String movieName = getArguments().getString(WatchlistApp.MOVIE_NAME);
        ArrayList<Credit> creditList = getArguments().getParcelableArrayList(WatchlistApp.CREDIT_LIST);

        toolbar.setTitle("");
        if (creditType == WatchlistApp.CREDIT_TYPE_CAST) {
            toolbarTitle.setText(R.string.cast_title);
        } else if (creditType == WatchlistApp.CREDIT_TYPE_CREW) {
            toolbarTitle.setText(R.string.crew_title);
        }
        toolbarSubtitle.setText(movieName);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.action_home));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        CreditAdapter adapter = new CreditAdapter(getContext(), creditList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        creditView.setHasFixedSize(true);
        creditView.setLayoutManager(layoutManager);
        creditView.setAdapter(adapter);

        return v;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // Click events
    @Override
    public void onCreditClicked(int position) {
        // TODO
    }
}
