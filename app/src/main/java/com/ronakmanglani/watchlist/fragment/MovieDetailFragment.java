package com.ronakmanglani.watchlist.fragment;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.activity.CreditActivity;
import com.ronakmanglani.watchlist.activity.PhotoActivity;
import com.ronakmanglani.watchlist.activity.ReviewActivity;
import com.ronakmanglani.watchlist.activity.VideoActivity;
import com.ronakmanglani.watchlist.database.MovieColumns;
import com.ronakmanglani.watchlist.database.MovieProvider;
import com.ronakmanglani.watchlist.model.Credit;
import com.ronakmanglani.watchlist.model.MovieDetail;
import com.ronakmanglani.watchlist.util.TMDBHelper;
import com.ronakmanglani.watchlist.util.VolleySingleton;
import com.ronakmanglani.watchlist.util.YoutubeHelper;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;
import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;
import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu.OnFloatingActionsMenuUpdateListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieDetailFragment extends Fragment implements
        OnMenuItemClickListener, OnFloatingActionsMenuUpdateListener {

    // Movie associated with the fragment
    private String id;
    private MovieDetail movie;
    private boolean isMovieWatched;
    private boolean isMovieToWatch;

    // Flags
    @BindBool(R.bool.is_tablet) boolean isTablet;
    private boolean isVideoAvailable = false;
    private boolean isFabMenuOpened = false;

    // Toolbar
    @Bind(R.id.toolbar)                 Toolbar toolbar;
    @Bind(R.id.toolbar_text_holder)     View toolbarTextHolder;
    @Bind(R.id.toolbar_title)           TextView toolbarTitle;
    @Bind(R.id.toolbar_subtitle)        TextView toolbarSubtitle;

    // Main views
    @Bind(R.id.progress_circle)         View progressCircle;
    @Bind(R.id.error_message)           View errorMessage;
    @Bind(R.id.movie_detail_holder)     View movieHolder;
    @Bind(R.id.fab_menu)                FloatingActionsMenu floatingActionsMenu;
    @Bind(R.id.fab_watched)             FloatingActionButton watchedButton;
    @Bind(R.id.fab_to_see)              FloatingActionButton toWatchButton;

    // Image views
    @Bind(R.id.backdrop_image)          NetworkImageView backdropImage;
    @Bind(R.id.backdrop_image_default)  ImageView backdropImageDefault;
    @Bind(R.id.backdrop_play_button)    View backdropPlayButton;
    @Bind(R.id.poster_image)            NetworkImageView posterImage;
    @Bind(R.id.poster_image_default)    ImageView posterImageDefault;

    // Basic info
    @Bind(R.id.movie_title)             TextView movieTitle;
    @Bind(R.id.movie_subtitle)          TextView movieSubtitle;
    @Bind(R.id.movie_rating_holder)     View movieRatingHolder;
    @Bind(R.id.movie_rating)            TextView movieRating;
    @Bind(R.id.movie_vote_count)        TextView movieVoteCount;

    // Overview
    @Bind(R.id.movie_overview_holder)   View movieOverviewHolder;
    @Bind(R.id.movie_overview_value)    TextView movieOverviewValue;

    // Crew
    @Bind(R.id.movie_crew_holder)       View movieCrewHolder;
    @Bind(R.id.movie_crew_see_all)      View movieCrewSeeAllButton;
    @Bind({R.id.movie_crew_value1, R.id.movie_crew_value2}) List<TextView> movieCrewValues;

    // Cast
    @Bind(R.id.movie_cast_holder)       View movieCastHolder;
    @Bind(R.id.movie_cast_see_all)      View movieCastSeeAllButton;
    @Bind({R.id.movie_cast_item1, R.id.movie_cast_item2, R.id.movie_cast_item3}) List<View> movieCastItems;
    @Bind({R.id.movie_cast_image1, R.id.movie_cast_image2, R.id.movie_cast_image3}) List<NetworkImageView> movieCastImages;
    @Bind({R.id.movie_cast_name1, R.id.movie_cast_name2, R.id.movie_cast_name3}) List<TextView> movieCastNames;
    @Bind({R.id.movie_cast_role1, R.id.movie_cast_role2, R.id.movie_cast_role3}) List<TextView> movieCastRoles;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, v);

        // Setup toolbar
        toolbar.setTitle(R.string.loading);
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

        // Download movie details if new instance, else restore from saved instance
        if (savedInstanceState == null || !(savedInstanceState.containsKey(Watchlist.MOVIE_ID)
                && savedInstanceState.containsKey(Watchlist.MOVIE_OBJECT))) {
            id = getArguments().getString(Watchlist.MOVIE_ID);
            if (id == null || id.equals("null")) {
                progressCircle.setVisibility(View.GONE);
                toolbarTextHolder.setVisibility(View.GONE);
                toolbar.setTitle("");
            } else {
                downloadMovieDetails(id);
            }
        } else {
            id = savedInstanceState.getString(Watchlist.MOVIE_ID);
            movie = savedInstanceState.getParcelable(Watchlist.MOVIE_OBJECT);
            onDownloadSuccessful();
        }

        // Setup FAB
        floatingActionsMenu.setOnFloatingActionsMenuUpdateListener(this);
        movieHolder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isFabMenuOpened) {
                    floatingActionsMenu.collapse();
                    return true;
                }
                return false;
            }
        });
        updateFabLabels(id);

        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (movie != null && id != null) {
            outState.putString(Watchlist.MOVIE_ID, id);
            outState.putParcelable(Watchlist.MOVIE_OBJECT, movie);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        VolleySingleton.getInstance(getActivity()).requestQueue.cancelAll(this.getClass().getName());
    }

    // Toolbar menu click
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            if (movie != null) {
                String shareText = getString(R.string.action_share_text, movie.title, TMDBHelper.getMovieShareURL(movie.id));
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, movie.title);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share_using)));
            }
            return true;
        } else {
            return false;
        }
    }

    // JSON parsing and display
    private void downloadMovieDetails(String id) {
        String urlToDownload = TMDBHelper.getMovieDetailLink(getActivity(), id);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, urlToDownload, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            String backdropImage = jsonObject.getString("backdrop_path");
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
                            String video = "";
                            JSONArray videoArray = jsonObject.getJSONObject("trailers").getJSONArray("youtube");
                            if (videoArray.length() > 0) {
                                video = videoArray.getJSONObject(0).getString("source");
                            }

                            movie = new MovieDetail(id, title, tagline, releaseDate, runtime, overview, voteAverage,
                                    voteCount, backdropImage, posterImage, video, cast, crew);

                            onDownloadSuccessful();

                        } catch (Exception ex) {
                            // Parsing error
                            onDownloadFailed();
                            Log.d("Parse Error", ex.getMessage(), ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // Network error
                        onDownloadFailed();
                    }
                });
        request.setTag(this.getClass().getName());
        VolleySingleton.getInstance(getActivity()).requestQueue.add(request);
    }
    private void onDownloadSuccessful() {

        // Toggle visibility
        progressCircle.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        movieHolder.setVisibility(View.VISIBLE);
        floatingActionsMenu.setVisibility(View.VISIBLE);

        // Set title and tagline
        if (movie.tagline == null || movie.tagline.equals("null") || movie.tagline.equals("")) {
            toolbar.setTitle(movie.title);
            toolbarTextHolder.setVisibility(View.GONE);
        } else {
            toolbar.setTitle("");
            toolbarTextHolder.setVisibility(View.VISIBLE);
            toolbarTitle.setText(movie.title);
            toolbarSubtitle.setText(movie.tagline);
        }

        // Add share button to toolbar
        toolbar.inflateMenu(R.menu.menu_share);

        // Backdrop image
        if (movie.backdropImage != null && !movie.backdropImage.equals("null") && !movie.backdropImage.equals("")) {
            int headerImageWidth = (int) getResources().getDimension(R.dimen.detail_backdrop_width);
            backdropImage.setImageUrl(TMDBHelper.getImageURL(movie.backdropImage, headerImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            if (movie.video.length() == 0) {
                isVideoAvailable = false;
            } else {
                backdropPlayButton.setVisibility(View.VISIBLE);
                isVideoAvailable = true;
            }
        } else {
            if (movie.video.length() == 0) {
                backdropImage.setVisibility(View.GONE);
                backdropImageDefault.setVisibility(View.VISIBLE);
                isVideoAvailable = false;
            } else {
                backdropImage.setImageUrl(YoutubeHelper.getThumbnailURL(movie.video),
                        VolleySingleton.getInstance(getActivity()).imageLoader);
                backdropPlayButton.setVisibility(View.VISIBLE);
                isVideoAvailable = true;
            }
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
            movieVoteCount.setText(getString(R.string.detail_vote_count, movie.voteCount));
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
            movieCrewValues.get(0).setText(getString(R.string.detail_crew_format, movie.crew.get(0).role, movie.crew.get(0).name));
            // Hide views
            movieCrewValues.get(1).setVisibility(View.GONE);
            movieCrewSeeAllButton.setVisibility(View.GONE);
            // Fix padding
            int padding = getResources().getDimensionPixelSize(R.dimen.dist_large);
            movieCrewHolder.setPadding(padding, padding, padding, padding);
        } else if (movie.crew.size() >= 2) {
            // Set values
            movieCrewValues.get(0).setText(getString(R.string.detail_crew_format, movie.crew.get(0).role, movie.crew.get(0).name));
            movieCrewValues.get(1).setText(getString(R.string.detail_crew_format, movie.crew.get(1).role, movie.crew.get(1).name));
            // Hide views
            if (movie.crew.size() == 2) {
                int padding = getResources().getDimensionPixelSize(R.dimen.dist_large);
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
            movieCastImages.get(0).setDefaultImageResId(R.drawable.default_cast);
            movieCastImages.get(0).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(0).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(0).setText(movie.cast.get(0).name);
            movieCastRoles.get(0).setText(movie.cast.get(0).role);
            // Hide views
            movieCastSeeAllButton.setVisibility(View.GONE);
            movieCastItems.get(2).setVisibility(View.GONE);
            movieCastItems.get(1).setVisibility(View.GONE);
            // Fix padding
            int padding = getResources().getDimensionPixelSize(R.dimen.dist_large);
            movieCastHolder.setPadding(padding, padding, padding, padding);
        } else if (movie.cast.size() == 2) {
            int castImageWidth = (int) getResources().getDimension(R.dimen.detail_cast_image_width);
            // 1
            movieCastImages.get(1).setDefaultImageResId(R.drawable.default_cast);
            movieCastImages.get(1).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(1).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(1).setText(movie.cast.get(1).name);
            movieCastRoles.get(1).setText(movie.cast.get(1).role);
            // 0
            movieCastImages.get(0).setDefaultImageResId(R.drawable.default_cast);
            movieCastImages.get(0).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(0).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(0).setText(movie.cast.get(0).name);
            movieCastRoles.get(0).setText(movie.cast.get(0).role);
            // Hide views
            movieCastSeeAllButton.setVisibility(View.GONE);
            movieCastItems.get(2).setVisibility(View.GONE);
            // Fix padding
            int padding = getResources().getDimensionPixelSize(R.dimen.dist_large);
            movieCastHolder.setPadding(padding, padding, padding, padding);
        } else if (movie.cast.size() >= 3) {
            int castImageWidth = (int) getResources().getDimension(R.dimen.detail_cast_image_width);
            // 2
            movieCastImages.get(2).setDefaultImageResId(R.drawable.default_cast);
            movieCastImages.get(2).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(2).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(2).setText(movie.cast.get(2).name);
            movieCastRoles.get(2).setText(movie.cast.get(2).role);
            // 1
            movieCastImages.get(1).setDefaultImageResId(R.drawable.default_cast);
            movieCastImages.get(1).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(1).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(1).setText(movie.cast.get(1).name);
            movieCastRoles.get(1).setText(movie.cast.get(1).role);
            // 0
            movieCastImages.get(0).setDefaultImageResId(R.drawable.default_cast);
            movieCastImages.get(0).setImageUrl(TMDBHelper.getImageURL(movie.cast.get(0).imagePath, castImageWidth),
                    VolleySingleton.getInstance(getActivity()).imageLoader);
            movieCastNames.get(0).setText(movie.cast.get(0).name);
            movieCastRoles.get(0).setText(movie.cast.get(0).role);
            // Hide show all button
            if (movie.cast.size() == 3) {
                int padding = getResources().getDimensionPixelSize(R.dimen.dist_large);
                movieCastHolder.setPadding(padding, padding, padding, padding);
                movieCastSeeAllButton.setVisibility(View.GONE);
            }
        }
    }
    private void onDownloadFailed() {
        errorMessage.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.GONE);
        movieHolder.setVisibility(View.GONE);
        toolbarTextHolder.setVisibility(View.GONE);
        toolbar.setTitle("");
    }

    // Click events
    @OnClick(R.id.button_photos)
    public void onPhotosButtonClicked() {
        Intent intent = new Intent(getContext(), PhotoActivity.class);
        intent.putExtra(Watchlist.MOVIE_ID, movie.id);
        intent.putExtra(Watchlist.MOVIE_NAME, movie.title);
        startActivity(intent);
    }
    @OnClick(R.id.button_reviews)
    public void onReviewsButtonClicked() {
        Intent intent = new Intent(getContext(), ReviewActivity.class);
        intent.putExtra(Watchlist.MOVIE_ID, movie.id);
        intent.putExtra(Watchlist.MOVIE_NAME, movie.title);
        startActivity(intent);
    }
    @OnClick(R.id.button_videos)
    public void onVideosButtonClicked() {
        Intent intent = new Intent(getContext(), VideoActivity.class);
        intent.putExtra(Watchlist.MOVIE_ID, movie.id);
        intent.putExtra(Watchlist.MOVIE_NAME, movie.title);
        startActivity(intent);
    }
    @OnClick(R.id.try_again)
    public void onTryAgainClicked() {
        errorMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);
        downloadMovieDetails(id);
    }
    @OnClick(R.id.backdrop_play_button)
    public void onTrailedPlayClicked() {
        if (isVideoAvailable) {
            try{
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + movie.video));
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + movie.video));
                startActivity(intent);
            }
        }
    }
    @OnClick(R.id.movie_crew_see_all)
    public void onSeeAllCrewClicked() {
        Intent intent = new Intent(getContext(), CreditActivity.class);
        intent.putExtra(Watchlist.CREDIT_TYPE, Watchlist.CREDIT_TYPE_CREW);
        intent.putExtra(Watchlist.MOVIE_NAME, movie.title);
        intent.putExtra(Watchlist.CREDIT_LIST, movie.crew);
        startActivity(intent);
    }
    @OnClick(R.id.movie_cast_see_all)
    public void onSeeAllCastClicked() {
        Intent intent = new Intent(getContext(), CreditActivity.class);
        intent.putExtra(Watchlist.CREDIT_TYPE, Watchlist.CREDIT_TYPE_CAST);
        intent.putExtra(Watchlist.MOVIE_NAME, movie.title);
        intent.putExtra(Watchlist.CREDIT_LIST, movie.cast);
        startActivity(intent);
    }
    @OnClick(R.id.movie_cast_item1)
    public void onFirstCastItemClicked() {
        // TODO
    }
    @OnClick(R.id.movie_cast_item2)
    public void onSecondCastItemClicked() {
        // TODO
    }
    @OnClick(R.id.movie_cast_item3)
    public void onThirdCastItemClicked() {
        // TODO
    }

    // FAB related functions
    private void updateFabLabels(final String movieId) {
        // Look in WATCHED table
        getLoaderManager().initLoader(42, null, new LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getContext(),
                        MovieProvider.Watched.CONTENT_URI,
                        new String[]{ },
                        MovieColumns.TMDB_ID + " = '" + movieId + "'",
                        null, null);
            }
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data.getCount() > 0) {
                    isMovieWatched = true;
                    watchedButton.setTitle(getString(R.string.detail_fab_watched_remove));
                }
            }
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
        // Look in TO_SEE table
        getLoaderManager().initLoader(43, null, new LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getContext(),
                        MovieProvider.ToSee.CONTENT_URI,
                        new String[]{ },
                        MovieColumns.TMDB_ID + " = '" + movieId + "'",
                        null, null);
            }
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data.getCount() > 0) {
                    isMovieToWatch = true;
                    toWatchButton.setTitle(getString(R.string.detail_fab_to_watch_remove));
                }
            }
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }
    @Override
    public void onMenuExpanded() {
        isFabMenuOpened = true;
    }
    @Override
    public void onMenuCollapsed() {
        isFabMenuOpened = false;
    }
    @OnClick(R.id.fab_watched)
    public void onWatchedButtonClicked() {
        if (!isMovieWatched) {
            ContentValues values = new ContentValues();
            values.put(MovieColumns.TMDB_ID, movie.id);
            values.put(MovieColumns.TITLE, movie.title);
            values.put(MovieColumns.YEAR, movie.getYear());
            values.put(MovieColumns.OVERVIEW, movie.overview);
            values.put(MovieColumns.RATING, movie.voteAverage);
            values.put(MovieColumns.POSTER, movie.posterImage);
            values.put(MovieColumns.BACKDROP, movie.backdropImage);
            getContext().getContentResolver().insert(MovieProvider.Watched.CONTENT_URI, values);
            Toast.makeText(getContext(), R.string.detail_watched_added, Toast.LENGTH_SHORT).show();
        } else {
            // TODO: Remove from database
        }
    }
    @OnClick(R.id.fab_to_see)
    public void onToWatchButtonClicked() {
        if (!isMovieToWatch) {
            ContentValues values = new ContentValues();
            values.put(MovieColumns.TMDB_ID, movie.id);
            values.put(MovieColumns.TITLE, movie.title);
            values.put(MovieColumns.YEAR, movie.getYear());
            values.put(MovieColumns.OVERVIEW, movie.overview);
            values.put(MovieColumns.RATING, movie.voteAverage);
            values.put(MovieColumns.POSTER, movie.posterImage);
            values.put(MovieColumns.BACKDROP, movie.backdropImage);
            getContext().getContentResolver().insert(MovieProvider.ToSee.CONTENT_URI, values);
            Toast.makeText(getContext(), R.string.detail_to_watch_added, Toast.LENGTH_SHORT).show();
        } else {
            // TODO: Remove from database
        }
    }
}