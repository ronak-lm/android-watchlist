package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ronakmanglani.watchlist.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReviewListActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @OnClick(R.id.write_review_button)
    public void onWriteReviewButtonClicked() {
        // TODO
    }
}
