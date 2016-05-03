package com.ronakmanglani.watchlist.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ronakmanglani.watchlist.BuildConfig;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.model.Review;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;

public class ReviewDetailFragment extends Fragment implements OnMenuItemClickListener  {

    @BindBool(R.bool.is_tablet) boolean isTablet;

    @Bind(R.id.toolbar)             Toolbar toolbar;
    @Bind(R.id.review_body)         TextView reviewBody;
    @Bind(R.id.review_body_holder)  View reviewBodyHolder;
    @Bind(R.id.review_large_ad)     AdView reviewAdView;

    private String movieName;
    private Review review;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_review_detail,container,false);
        ButterKnife.bind(this, v);

        // Get arguments
        movieName = getArguments().getString(Watchlist.MOVIE_NAME);
        review = getArguments().getParcelable(Watchlist.REVIEW_OBJECT);
        if (review == null) {
            if (movieName.equals("null")) {
                toolbar.setTitle("");
            } else {
                toolbar.setTitle(R.string.loading);
            }
            reviewBodyHolder.setVisibility(View.GONE);
        } else {
            // Setup toolbar
            toolbar.setTitle("Review by " + review.author);
            toolbar.inflateMenu(R.menu.menu_share);
            toolbar.setOnMenuItemClickListener(this);
            if (!isTablet) {
                toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.action_home));
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().finish();
                    }
                });
            }
            // Set review body
            reviewBody.setText(review.body);
        }

        // Load Ad
        AdRequest.Builder adBuilder = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            adBuilder.addTestDevice(getString(R.string.yu_yuphoria_id))
                     .addTestDevice(getString(R.string.genymotion_tablet_id));
        }
        AdRequest adRequest = adBuilder.build();
        reviewAdView.loadAd(adRequest);
        reviewAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                reviewAdView.setVisibility(View.GONE);
            }
        });

        return v;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    // Toolbar options menu
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            String shareText = "A review of " + movieName + " by " + review.author + " - " + review.url;
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, movieName + " - Review");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share_using)));
            return true;
        } else {
            return false;
        }
    }
}