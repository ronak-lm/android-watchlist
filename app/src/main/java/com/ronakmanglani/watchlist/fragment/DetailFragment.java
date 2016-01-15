package com.ronakmanglani.watchlist.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.ronakmanglani.watchlist.activity.DetailActivity;
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
    @Bind(R.id.movie_rating) TextView movieRating;

    // Overview
    @Bind(R.id.movie_overview_holder) View movieOverviewHolder;
    @Bind(R.id.movie_overview_value) TextView movieOverviewValue;

    // Crew
    @Bind(R.id.movie_crew_holder) View movieCrewHolder;
    @Bind({R.id.movie_crew_value1, R.id.movie_crew_value2}) List<TextView> movieCrewValues;
    @Bind(R.id.movie_crew_see_all) View movieCrewSeeAllButton;

    // Cast
    @Bind(R.id.movie_cast_holder) View movieCastHolder;
    @Bind({R.id.movie_cast_item1, R.id.movie_cast_item2, R.id.movie_cast_item3}) List<View> movieCastItems;
    @Bind({R.id.movie_cast_image1, R.id.movie_cast_image2, R.id.movie_cast_image3}) List<NetworkImageView> movieCastImages;
    @Bind({R.id.movie_cast_name1, R.id.movie_cast_name2, R.id.movie_cast_name3}) List<TextView> movieCastNames;
    @Bind({R.id.movie_cast_role1, R.id.movie_cast_role2, R.id.movie_cast_role3}) List<TextView> movieCastRoles;
    @Bind(R.id.movie_cast_see_all) View movieCastSeeAllButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, v);

        // Set loading as title
        toolbar.setTitle(R.string.loading);

        // Download movie details
        id = getArguments().getString(DetailActivity.MOVIE_ID);
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
                            String tagline = jsonObject.getString("tagline");
                            String title = jsonObject.getString("title");
                            String voteAverage = jsonObject.getString("vote_average");
                            String voteCount = jsonObject.getString("vote_count");
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
                            ArrayList<String> images = new ArrayList<>();
                            JSONArray imageArray = jsonObject.getJSONObject("images").getJSONArray("backdrops");
                            for (int i = 0; i < imageArray.length(); i++) {
                                JSONObject object = (JSONObject) imageArray.get(i);
                                String url = object.getString("file_path");
                                images.add(url);
                            }
                            ArrayList<String> videos = new ArrayList<>();
                            JSONArray videoArray = jsonObject.getJSONObject("trailers").getJSONArray("youtube");
                            for (int i = 0; i < videoArray.length(); i++) {
                                JSONObject object = (JSONObject) videoArray.get(i);
                                String videoID = object.getString("source");
                                videos.add(videoID);
                            }

                            // Create movie object
                            movie = new MovieDetail(id, title, tagline, releaseDate, runtime, overview, voteAverage,
                                    voteCount, genre, backdropImage, posterImage, images, videos, cast, crew);

                            // Bind class to layout views
                            onDownloadSuccessful();

                        } catch (Exception ex) {
                            // Show error message on parsing errors
                            onDownloadFailed();
                            Log.d("ParseError", ex.getMessage(), ex);
                        }
                    }
                },
                // Show error message on network errors
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onDownloadFailed();
                    }
                });

        // Set thread tags for reference
        request.setTag(this.getClass().getName());

        // Add download request to queue
        VolleySingleton.getInstance(getActivity()).requestQueue.add(request);
    }

    // Bind movie class attribute to layout views
    private void onDownloadSuccessful() {

        // Toggle visibility
        progressCircle.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        movieHolder.setVisibility(View.VISIBLE);

        // Set title
        toolbar.setTitle(movie.title);

        // Backdrop image
        if (movie.backdropImage != null && !movie.backdropImage.equals("null")) {
            int headerImageWidth = (int) getResources().getDimension(R.dimen.detail_backdrop_width);
            backdropImage.setImageUrl(TMDBHelper.getImageURL(movie.backdropImage, headerImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
        } else {
            backdropImageDefault.setVisibility(View.VISIBLE);
            backdropImage.setVisibility(View.GONE);
        }

        // Basic info
        if (movie.posterImage != null && !movie.posterImage.equals("null")) {
            int posterImageWidth = (int) getResources().getDimension(R.dimen.movie_detail_poster_width);
            posterImage.setImageUrl(TMDBHelper.getImageURL(movie.posterImage, posterImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
        } else {
            posterImageDefault.setVisibility(View.VISIBLE);
            posterImage.setVisibility(View.GONE);
        }
        movieTitle.setText(movie.title);
        movieSubtitle.setText(movie.getSubtitle());
        if (movie.voteAverage == null || movie.voteAverage.equals("null") || movie.voteAverage.equals("0.0")) {
            movieRatingHolder.setVisibility(View.GONE);
        } else {
            movieRating.setText(movie.voteAverage);
        }

        // Overview
        if (movie.overview != null && !movie.overview.equals("null")) {
            movieOverviewValue.setText(movie.overview);
        } else {
            movieOverviewHolder.setVisibility(View.GONE);
        }

        // Crew
        if (movie.crew.size() == 0) {
            movieCrewHolder.setVisibility(View.GONE);
        } else if (movie.crew.size() == 1) {
            // Set value
            movieCrewValues.get(0).setText(movie.crew.get(0).role + ": " + movie.crew.get(0).name);
            // Hide views
            movieCrewValues.get(1).setVisibility(View.GONE);
            movieCrewSeeAllButton.setVisibility(View.GONE);
            // Fix padding
            int padding = getResources().getDimensionPixelSize(R.dimen.large_margin);
            movieCrewHolder.setPadding(padding, padding, padding, padding);
        } else if (movie.crew.size() >= 2) {
            // Set values
            movieCrewValues.get(0).setText(movie.crew.get(0).role + ": " + movie.crew.get(0).name);
            movieCrewValues.get(1).setText(movie.crew.get(1).role + ": " + movie.crew.get(1).name);
            // Hide views
            if (movie.crew.size() == 2) {
                int padding = getResources().getDimensionPixelSize(R.dimen.large_margin);
                movieCrewHolder.setPadding(padding, padding, padding, padding);
                movieCrewSeeAllButton.setVisibility(View.GONE);
            }
        }

        // Cast
        if (movie.cast.size() == 0) {
            movieCastHolder.setVisibility(View.GONE);
        } else if (movie.cast.size() == 1) {
            int castImageWidth = (int) getResources().getDimension(R.dimen.detail_cast_image_width);
            // 0
            movieCastImages.get(0).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(0).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(0).setText(movie.cast.get(0).name);
            movieCastRoles.get(0).setText(movie.cast.get(0).role);
            // Hide views
            movieCastSeeAllButton.setVisibility(View.GONE);
            movieCastItems.get(2).setVisibility(View.GONE);
            movieCastItems.get(1).setVisibility(View.GONE);
            // Fix padding
            int padding = getResources().getDimensionPixelSize(R.dimen.large_margin);
            movieCastHolder.setPadding(padding, padding, padding, padding);
        } else if (movie.cast.size() == 2) {
            int castImageWidth = (int) getResources().getDimension(R.dimen.detail_cast_image_width);
            // 1
            movieCastImages.get(1).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(1).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(1).setText(movie.cast.get(1).name);
            movieCastRoles.get(1).setText(movie.cast.get(1).role);
            // 0
            movieCastImages.get(0).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(0).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(0).setText(movie.cast.get(0).name);
            movieCastRoles.get(0).setText(movie.cast.get(0).role);
            // Hide views
            movieCastSeeAllButton.setVisibility(View.GONE);
            movieCastItems.get(2).setVisibility(View.GONE);
            // Fix padding
            int padding = getResources().getDimensionPixelSize(R.dimen.large_margin);
            movieCastHolder.setPadding(padding, padding, padding, padding);
        } else if (movie.cast.size() >= 3) {
            int castImageWidth = (int) getResources().getDimension(R.dimen.detail_cast_image_width);
            // 2
            movieCastImages.get(2).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(2).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(2).setText(movie.cast.get(2).name);
            movieCastRoles.get(2).setText(movie.cast.get(2).role);
            // 1
            movieCastImages.get(1).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(1).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(1).setText(movie.cast.get(1).name);
            movieCastRoles.get(1).setText(movie.cast.get(1).role);
            // 0
            movieCastImages.get(0).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(0).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(0).setText(movie.cast.get(0).name);
            movieCastRoles.get(0).setText(movie.cast.get(0).role);
            // Hide show all button
            if (movie.cast.size() == 3) {
                int padding = getResources().getDimensionPixelSize(R.dimen.large_margin);
                movieCastHolder.setPadding(padding, padding, padding, padding);
                movieCastSeeAllButton.setVisibility(View.GONE);
            }
        }
    }

    // Show error message when download or parsing failed
    private void onDownloadFailed() {
        errorMessage.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.GONE);
        movieHolder.setVisibility(View.GONE);
    }

    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);
        downloadMovieDetails(id);
    }

    @OnClick(R.id.movie_crew_see_all)
    public void onSeeAllCrewClicked() {
        // TODO
    }

    @OnClick(R.id.movie_cast_see_all)
    public void onSeeAllCastClicked() {
        // TODO
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
