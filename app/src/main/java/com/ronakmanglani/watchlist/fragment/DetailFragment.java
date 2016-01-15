package com.ronakmanglani.watchlist.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.model.Credit;
import com.ronakmanglani.watchlist.model.MovieDetail;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailFragment extends Fragment {

    // Key for intent extra
    public static final String MOVIE_ID = "movie_id";

    // Movie associated with the activity
    private String id;
    private MovieDetail movie;

    // Main views
    @Bind(R.id.toolbar) Toolbar toolbar;
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

        // Set loading as title
        toolbar.setTitle(R.string.loading);

        // Download movie details
        id = savedInstanceState.getString(MOVIE_ID);
        downloadMovieDetails(id);

        return v;
    }

    // Download Movie Detail from TMDB
    private void downloadMovieDetails(String id) {
        String urlToDownload = TMDBHelper.getMovieDetailLink(getActivity(), id);
        JsonObjectRequest request = new JsonObjectRequest(
                // Request method and URL to be downloaded
                Request.Method.GET, urlToDownload, null,
                // To respond when JSON gets downloaded
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            // Parse JSON object to Movie
                            String backdropImage = jsonObject.getString("backdrop_path");
                            ArrayList<String> genre = new ArrayList<>();
                            JSONArray genreArray = jsonObject.getJSONArray("genres");
                            for (int i = 0; i < genreArray.length(); i++) {
                                genre.add(((JSONObject) genreArray.get(i)).getString("name"));
                            }
                            String id = jsonObject.getString("id");
                            String overview = jsonObject.getString("overview");
                            String posterImage = jsonObject.getString("poster_path");
                            String releaseDate = jsonObject.getString("release_date");
                            String runtime = jsonObject.getString("runtime");
                            String title = jsonObject.getString("title");
                            String voteAverage = jsonObject.getString("vote_average");
                            String voteCount = jsonObject.getString("vote_count");
                            ArrayList<String> videos = new ArrayList<>();
                            JSONArray videoArray = jsonObject.getJSONObject("trailers").getJSONArray("youtube");
                            for (int i = 0; i < videoArray.length(); i++) {
                                JSONObject object = (JSONObject) videoArray.get(i);
                                String videoID = object.getString("source");
                                videos.add(videoID);
                            }
                            ArrayList<Credit> cast = new ArrayList<>();
                            JSONArray castArray = jsonObject.getJSONObject("credits").getJSONArray("cast");
                            for (int i = 0; i < castArray.length(); i++) {
                                JSONObject object = (JSONObject) castArray.get(i);
                                String role = object.getString("character");
                                String person_id = object.getString("id");
                                String name = object.getString("name");
                                String profileImage = object.getString("profile_path");
                                cast.add(new Credit(person_id, name, role, profileImage));
                            }
                            ArrayList<Credit> crew = new ArrayList<>();
                            JSONArray crewArray = jsonObject.getJSONObject("credits").getJSONArray("crew");
                            for (int i = 0; i < crewArray.length(); i++) {
                                JSONObject object = (JSONObject) crewArray.get(i);
                                String person_id = object.getString("id");
                                String role = object.getString("job");
                                String name = object.getString("name");
                                String profileImage = object.getString("profile_path");
                                crew.add(new Credit(person_id, name, role, profileImage));
                            }
                            // Create movie object
                            movie = new MovieDetail(id, title, releaseDate, runtime, overview, voteAverage,
                                    voteCount, genre, backdropImage, posterImage, videos, cast, crew);
                            // TODO: Parse backdrop images and tagline
                            // TODO: Update UI
                        } catch (Exception ex) {
                            // Show error message on parsing errors
                            showErrorMessage();
                        }
                    }
                },
                // Show error message on network errors
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showErrorMessage();
                    }
                });

        // Set thread tags for reference
        request.setTag(this.getClass().getName());

        // Add download request to queue
        VolleySingleton.getInstance(getActivity()).requestQueue.add(request);
    }

    // To show error message when download or parsing failed
    private void showErrorMessage() {
        errorMessage.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.GONE);
        movieHolder.setVisibility(View.GONE);
    }

    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);
        // downloadMovieDetails(id);
    }

    @OnClick(R.id.movie_crew_see_all)
    public void onSeeAllCrewClicked() {

    }

    @OnClick(R.id.movie_cast_see_all)
    public void onSeeAllCastClicked() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
