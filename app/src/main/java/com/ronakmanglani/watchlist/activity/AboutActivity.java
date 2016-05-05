package com.ronakmanglani.watchlist.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends AppCompatActivity {

    private Tracker tracker;
    @Bind(R.id.toolbar) Toolbar toolbar;

    // Activity Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load Analytics Tracker
        tracker = ((Watchlist) getApplication()).getTracker();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Send screen name to analytics
        tracker.setScreenName(getString(R.string.screen_about));
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // Toolbar Home/Back Button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return false;
        }
    }

    // Floating Buttons
    @OnClick(R.id.fab_rate)
    public void onRateButtonClicked() {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        startActivity(rateIntent);
    }
    @OnClick(R.id.fab_email)
    public void onEmailButtonClicked() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","ronak_lm@outlook.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.about_email_using)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.about_email_error, Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.fab_twitter)
    public void onTwitterButtonClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/Ronak_LM"));
        startActivity(browserIntent);
    }
}
