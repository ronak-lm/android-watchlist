package com.ronakmanglani.watchlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.adapter.MovieDetailVideoAdapter;
import com.ronakmanglani.watchlist.model.Credit;
import com.ronakmanglani.watchlist.model.MovieDetail;
import com.ronakmanglani.watchlist.model.Video;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.util.YoutubeHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {

    // Key for intent extra
    public static final String MOVIE_ID = "movie_id";

    // Movie associated with the activity
    private String id;
    private MovieDetail movie;

    // AppBar Views
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout toolbarLayout;
    private ImageView defaultHeaderImage;
    private NetworkImageView movieHeaderImage;

    // Movie Views
    private View movieContainer;
    private ImageView defaultPosterImage;
    private NetworkImageView moviePosterImage;
    private TextView movieTitle;
    private TextView movieSubtitle;
    private TextView movieRating;
    private TextView moviePlot;
    private RecyclerView movieVideos;
    private MovieDetailVideoAdapter videoAdapter;

    // Error message and loading circle
    private View errorMessage;
    private View progressCircle;

    // Activity start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find Appbar views
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbarLayout = (CollapsingToolbarLayout) appBarLayout.findViewById(R.id.collapsing_toolbar);
        defaultHeaderImage = (ImageView) toolbarLayout.findViewById(R.id.header_image_default);
        movieHeaderImage = (NetworkImageView) toolbarLayout.findViewById(R.id.header_image);
        movieHeaderImage.setColorFilter(ContextCompat.getColor(this, R.color.image_tint));

        // Find movie views
        movieContainer = findViewById(R.id.movie_container);
        movieContainer.setVisibility(View.GONE);
        defaultPosterImage = (ImageView) movieContainer.findViewById(R.id.poster_image_default);
        moviePosterImage = (NetworkImageView) movieContainer.findViewById(R.id.poster_image);
        movieTitle = (TextView) movieContainer.findViewById(R.id.movie_title);
        movieSubtitle = (TextView) movieContainer.findViewById(R.id.movie_subtitle);
        movieRating = (TextView) movieContainer.findViewById(R.id.movie_rating);
        moviePlot = (TextView) movieContainer.findViewById(R.id.movie_plot);

        // Trailer Views
        ArrayList<Video> videos = new ArrayList<>();
        movieVideos = (RecyclerView) movieContainer.findViewById(R.id.movie_videos);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        videoAdapter = new MovieDetailVideoAdapter(this, videos, onItemClickListener);
        movieVideos.setHasFixedSize(true);
        movieVideos.setLayoutManager(layoutManager);
        movieVideos.setAdapter(videoAdapter);

        // Find error and loading circle
        errorMessage = findViewById(R.id.error_message);
        progressCircle = findViewById(R.id.progress_circle);

        // Set listeners
        errorMessage.findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorMessage.setVisibility(View.GONE);
                progressCircle.setVisibility(View.VISIBLE);
                appBarLayout.setVisibility(View.VISIBLE);
                downloadMovieDetails(id);
            }
        });

        // Download movie data from TMDB
        id = getIntent().getStringExtra(MOVIE_ID);
        downloadMovieDetails(id);
    }

    // Toolbar menu functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                String shareText = getString(R.string.action_share_text) + " " + movie.title + " - " + TMDBHelper.getMovieShareURL(movie.id);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, movie.title);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share_using)));
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    // Cancel any pending network requests when activity stops
    @Override
    public void onStop () {
        super.onStop();
        VolleySingleton.getInstance(this).requestQueue.cancelAll(this.getClass().getName());
    }

    // Download Movie Detail from TMDB
    private void downloadMovieDetails(String id) {
        String urlToDownload = TMDBHelper.getMovieDetailLink(this, id);
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
                            // Download trailers
                            downloadVideoDetails();
                            // Update the UI
                            progressCircle.setVisibility(View.GONE);
                            errorMessage.setVisibility(View.GONE);
                            movieContainer.setVisibility(View.VISIBLE);
                            // Fill data
                            toolbarLayout.setTitle(movie.title);
                            if (movie.backdropImage == null || movie.backdropImage.equals("null")) {
                                defaultHeaderImage.setVisibility(View.VISIBLE);
                                movieHeaderImage.setVisibility(View.GONE);
                            } else {
                                int headerImageWidth = (int) getResources().getDimension(R.dimen.movie_header_width);
                                movieHeaderImage.setImageUrl(TMDBHelper.getImageURL(movie.backdropImage, headerImageWidth),
                                                            VolleySingleton.getInstance(getApplicationContext()).imageLoader);
                            }
                            if (movie.posterImage == null || movie.posterImage.equals("null")) {
                                defaultPosterImage.setVisibility(View.VISIBLE);
                                moviePosterImage.setVisibility(View.GONE);
                            } else {
                                int posterImageWidth = (int) getResources().getDimension(R.dimen.movie_detail_poster_width);
                                moviePosterImage.setImageUrl(TMDBHelper.getImageURL(movie.posterImage, posterImageWidth),
                                                            VolleySingleton.getInstance(getApplicationContext()).imageLoader);
                            }
                            movieTitle.setText(movie.title);
                            movieSubtitle.setText(movie.getSubtitle());
                            if (movie.voteAverage == null || movie.voteAverage.equals("null") || movie.voteAverage.equals("0.0")) {
                                findViewById(R.id.movie_rating_holder).setVisibility(View.GONE);
                            } else {
                                movieRating.setText(movie.voteAverage);
                            }
                            if (movie.overview == null || movie.overview.equals("null")) {
                                findViewById(R.id.movie_plot_holder).setVisibility(View.GONE);
                            } else {
                                moviePlot.setText(movie.overview);
                            }
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
        VolleySingleton.getInstance(this).requestQueue.add(request);
    }
    // Download video details from Youtube
    private void downloadVideoDetails() {
        // Check if no videos available
        if (movie.videos.size() == 0) {
            findViewById(R.id.movie_video_holder).setVisibility(View.GONE);
        } else {
            // Initialize ArrayList
            if (videoAdapter.videos == null) {
                videoAdapter.videos = new ArrayList<>();
            }
            // Loop through all videos
            for (int i = 0; i < movie.videos.size(); i++) {
                final int currentPosition = i;
                String urlToDownload = YoutubeHelper.getDetailURL(movie.videos.get(currentPosition));
                JsonObjectRequest request = new JsonObjectRequest(
                        // Request method and URL to be downloaded
                        Request.Method.GET, urlToDownload, null,
                        // To respond when JSON gets downloaded
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    String title = jsonObject.getString("title");
                                    String youtubeID = movie.videos.get(currentPosition);
                                    String imageURL = YoutubeHelper.getThumbnailURL(youtubeID);
                                    String videoURL = YoutubeHelper.getVideoURL(youtubeID);
                                    Video video = new Video(title, youtubeID, imageURL, videoURL);
                                    videoAdapter.videos.add(video);
                                    videoAdapter.notifyDataSetChanged();
                                } catch (Exception ex) {
                                    // Parsing errors - Do nothing
                                }
                            }
                        },
                        // Error Listener
                        null);

                // Set thread tags for reference
                request.setTag(this.getClass().getName());

                // Add download request to queue
                VolleySingleton.getInstance(this).requestQueue.add(request);
            }
        }
    }
    // To show error message when download or parsing failed
    private void showErrorMessage() {
        errorMessage.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.GONE);
        movieContainer.setVisibility(View.GONE);
        appBarLayout.setVisibility(View.GONE);
    }

    // Click listner for videos
    MovieDetailVideoAdapter.OnItemClickListener onItemClickListener = new MovieDetailVideoAdapter.OnItemClickListener() {
        @Override
        public void onItemClicked(int position) {
            // TODO: Respond to click
        }
    };
}
