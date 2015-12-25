package com.ronakmanglani.watchlist.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.adapter.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {

    // Key for SharedPreferences
    private static String LAST_PAGE;

    // Start activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        final SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        LAST_PAGE = getString(R.string.settings_last_page);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup ViewPager
        final MainPagerAdapter adapter = new MainPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) { }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                // Set activity title
                setTitle(adapter.getTitle(position));
                // Save selected page to preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(LAST_PAGE, position);
                editor.apply();
            }
        });
        viewPager.setCurrentItem(sharedPref.getInt(LAST_PAGE, 0));
        if (viewPager.getCurrentItem() == 0) {
            setTitle(adapter.getTitle(0));
        }

        // Setup TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < adapter.getCount(); i++) {
            tabLayout.getTabAt(i).setIcon(adapter.getIcon(i));
        }
    }

    // Toolbar menu functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Toast.makeText(this, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_about:
                Toast.makeText(this, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
