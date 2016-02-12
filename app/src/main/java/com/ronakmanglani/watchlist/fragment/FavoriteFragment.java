package com.ronakmanglani.watchlist.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.activity.DetailActivity;
import com.ronakmanglani.watchlist.activity.MainActivity;
import com.ronakmanglani.watchlist.adapter.BaseMovieAdapter;
import com.ronakmanglani.watchlist.database.MovieDB;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;

public class FavoriteFragment extends Fragment implements BaseMovieAdapter.OnMovieClickListener {

    private Context context;

    // Flag variable
    @BindBool(R.bool.is_tablet) boolean isTablet;

    // Layout views
    @Bind(R.id.no_favorites) View noFavorites;
    @Bind(R.id.progress_circle) View progressCircle;
    @Bind(R.id.movie_grid) RecyclerView recyclerView;

    // Adapter for RecyclerView
    private BaseMovieAdapter adapter;

    // Fragment lifecycle methods
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite, container, false);
        ButterKnife.bind(this, v);
        context = getContext();

        // Initialize database
        MovieDB database = MovieDB.getInstance(context);

        // Update UI
        if (database.movieList.size() == 0) {
            progressCircle.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            noFavorites.setVisibility(View.VISIBLE);
            // Update detail fragment for tablet mode
            if (isTablet) {
                ((MainActivity)getActivity()).loadDetailFragmentWith("null");
            }
        } else {
            // Setup RecyclerView
            adapter = new BaseMovieAdapter(context, this);
            adapter.movieList = database.movieList;
            GridLayoutManager layoutManager = new GridLayoutManager(context, getNumberOfColumns());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            // Update UI
            progressCircle.setVisibility(View.GONE);
            noFavorites.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            // Update detail fragment for tablet mode
            if (isTablet) {
                ((MainActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(0).id);
            }
        }

        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    // Helper method
    public int getNumberOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPx = displayMetrics.widthPixels;
        if (isTablet) {
            widthPx = widthPx / 3;
        }
        float desiredPx = getResources().getDimensionPixelSize(R.dimen.movie_card_width);
        int columns = Math.round(widthPx / desiredPx);
        return columns > 2 ? columns : 2;
    }

    // OnClick method
    @Override
    public void onMovieClicked(int position) {
        if (isTablet) {
            ((MainActivity)getActivity()).loadDetailFragmentWith(adapter.movieList.get(position).id);
        } else {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.MOVIE_ID, adapter.movieList.get(position).id);
            startActivity(intent);
        }
    }
}
