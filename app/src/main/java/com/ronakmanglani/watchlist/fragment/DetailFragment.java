package com.ronakmanglani.watchlist.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailFragment extends Fragment {

    // Main views
    @Bind(R.id.progress_circle) View progressCircle;
    @Bind(R.id.error_message) View errorMessage;
    @Bind(R.id.movie_detail_holder) View movieHolder;

    // Image views
    @Bind(R.id.backdrop_image_default) ImageView backdropImageDefault;
    @Bind(R.id.backdrop_image) NetworkImageView backdropImage;
    @Bind(R.id.poster_image_default) ImageView posterImageDefault;
    @Bind(R.id.poster_image) NetworkImageView posterImage;

    // Basic info
    @Bind(R.id.movie_title) TextView movieTitle;
    @Bind(R.id.movie_subtitle) TextView movieSubtitle;
    @Bind(R.id.movie_rating_holder) View movieRatingHolder;
    @Bind(R.id.movie_rating) View movieRating;

    // Overview
    @Bind(R.id.movie_overview_holder) View movieOverviewHolder;
    @Bind(R.id.movie_overview_value) TextView movieOverviewValue;

    // Crew views
    @Bind(R.id.movie_crew_holder) View movieCrewHolder;
    @Bind(R.id.movie_crew_value1) TextView movieCrewValue1;
    @Bind(R.id.movie_crew_value2) TextView movieCrewValue2;

    // Cast views
    @Bind(R.id.movie_cast_holder) View movieCastHolder;
    @Bind({R.id.movie_cast_item1, R.id.movie_cast_item2, R.id.movie_cast_item3}) List<View> movieCastItems;
    @Bind({R.id.movie_cast_image1, R.id.movie_cast_image2, R.id.movie_cast_image3}) List<NetworkImageView> movieCastImages;
    @Bind({R.id.movie_cast_name1, R.id.movie_cast_name2, R.id.movie_cast_name3}) List<TextView> movieCastNames;
    @Bind({R.id.movie_cast_role1, R.id.movie_cast_role2, R.id.movie_cast_role3}) List<TextView> movieCastRoles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @OnClick(R.id.movie_crew_see_all)
    public void seeAllCrew() {

    }

    @OnClick(R.id.movie_cast_see_all)
    public void seeAllCast() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
