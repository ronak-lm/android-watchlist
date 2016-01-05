package com.ronakmanglani.watchlist.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.ronakmanglani.watchlist.R;

import butterknife.Bind;

public class DrawerActivity extends AppCompatActivity {

    private String[] drawerList;
    @Bind(R.id.drawer_layout)
    private DrawerLayout drawerLayout;
    @Bind(R.id.drawer_recycler)
    private RecyclerView recyclerView;

    // Start activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        drawerList = getResources().getStringArray(R.array.drawer_list);
    }
}
