package com.example.amitava.moviestalker;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.example.amitava.moviestalker.Data.FavoriteListContract.FavoriteListEntry;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.amitava.moviestalker.Data.FavoriteListContract;
import com.example.amitava.moviestalker.Data.FavoriteListDbHelper;
import com.example.amitava.moviestalker.Utilities.ListItem;
import com.example.amitava.moviestalker.Utilities.NetworkUtilities;
import com.example.amitava.moviestalker.Utilities.ReviewsAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Amitava on 03-Dec-17.
 */

public class DetailsActivity extends AppCompatActivity {
    private TextView mTitle;
    private ImageView mPoster;
    private TextView mReleaseDate;
    private TextView mRating;
    private TextView mSynopsis;
    private Button mTrailer;

    private ScrollView wholePage;
    private ProgressBar progressBar;

    private ListView mReviews;
    private ReviewsAdapter reviewsAdapter;
    ArrayList<ListItem> mListData;

    private SQLiteDatabase database;
    private boolean isFavorite = false;
    private Menu menuFav;

    private String title;
    private String image;
    private String releaseDate;
    private String rating;
    private String overview;

    private int id;
    private String sessionId;
    private URL trailerUrl;
    private URL reviewUrl;

    private Intent trailerAppIntent = null;
    private Intent trailerWebIntent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details_layout);

        progressBar = (ProgressBar) findViewById(R.id.pb_reviews_loading_indicator);
        wholePage = (ScrollView) findViewById(R.id.movie_details_layout);

        title = getIntent().getStringExtra("title");
        image = getIntent().getStringExtra("image");
        releaseDate = getIntent().getStringExtra("release_date");
        rating = getIntent().getStringExtra("rating");
        overview = getIntent().getStringExtra("synopsis");

        sessionId = getIntent().getStringExtra("session_id");

        id = getIntent().getIntExtra("id", 0);
        //Creating the URL for fetching Trailer
        trailerUrl = NetworkUtilities.buildTrailerUrl(Integer.toString(id));
        reviewUrl = NetworkUtilities.buildReviewsUrl(Integer.toString(id));

        mTitle = (TextView) findViewById(R.id.tv_movie_title);
        mPoster = (ImageView) findViewById(R.id.iv_movie_poster);
        mReleaseDate = (TextView) findViewById(R.id.tv_movie_release_date);
        mRating = (TextView) findViewById(R.id.tv_movie_rating);
        mSynopsis = (TextView) findViewById(R.id.tv_movie_synopsis);
        mTrailer = (Button) findViewById(R.id.play_trailer);
        mTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playTrailerForMovie(view);
            }
        });
        mReviews = (ListView) findViewById(R.id.lv_user_reviews);
        mReviews.setOnTouchListener(new View.OnTouchListener() {
            //Setting on Touch Listener for handling the Touch inside ScrollView
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Deactivate the touch request for parent scroll on touch of child
                //So that ListView scroll can function properly
                view.getParent().requestDisallowInterceptTouchEvent(true);
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                return false;
            }
        });
        //Initializing the Review List with empty adapter
        mListData = new ArrayList<ListItem>();
        reviewsAdapter = new ReviewsAdapter(this, R.layout.review_row, mListData);
        mReviews.setAdapter(reviewsAdapter);

        mTitle.setText(title);
        Picasso.with(this).load(image).into(mPoster);
        mReleaseDate.setText(releaseDate);
        mRating.setText(rating);
        mSynopsis.setText(overview);
        //Checking if the movie is favorite
        isFavorite =  checkIfFavorite(database);
        if (menuFav != null){
            MenuItem menuItem = menuFav.findItem(R.id.mn_add_to_fav);
            if (isFavorite){
                changeMenuColor(menuItem, getResources().getColor(R.color.favorite));
            } else {
                changeMenuColor(menuItem, getResources().getColor(R.color.notFavorite));
            }
        }
        new MovieTrailerReviewQueryTask().execute(trailerUrl, reviewUrl);
        //COMPLETED 3: Write the code for fetching reviews for the respective movie and display in ListView
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_to_favorite_movie_details, menu);
        menuFav = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.mn_add_to_fav);
        if (isFavorite){
            changeMenuColor(item, getResources().getColor(R.color.favorite));
        } else {
            changeMenuColor(item, getResources().getColor(R.color.notFavorite));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mn_add_to_fav){
            if (!isFavorite){
                //COMPLETED 1: Write the code for adding the movie to Favorite and preferably in SQLiteDatabase
                //Insert movie information to the database
                FavoriteListDbHelper favoriteListDbHelper = new FavoriteListDbHelper(this);
                database = favoriteListDbHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(FavoriteListEntry._ID, id);
                contentValues.put(FavoriteListEntry.COLUMN_MOVIE_TITLE, title);
                long rows = database.insert(FavoriteListEntry.TABLE_NAME, null, contentValues);
                database.close();
                Log.e("Data Insert", "Rows inserted: " + rows);
                URL makeFavoriteUrl = NetworkUtilities.buildFavoriteMovieUrl(id, sessionId);
                new SetMovieAsFavorite().execute(makeFavoriteUrl);

                changeMenuColor(item, getResources().getColor(R.color.favorite));
            } else {
                //Code for removing the movie from favorites database and from the site
                FavoriteListDbHelper favoriteListDbHelper = new FavoriteListDbHelper(this);
                database = favoriteListDbHelper.getWritableDatabase();
                database.execSQL("DELETE FROM " + FavoriteListEntry.TABLE_NAME + " WHERE " +
                        FavoriteListEntry._ID + "=" + String.valueOf(id));
                database.close();

                //Code for cancelling the favorite(rating) from the site
                URL cancelFavoriteUrl = NetworkUtilities.buildUrlForUnfavorite(id, sessionId);
                new CancelFavoriteMovie().execute(cancelFavoriteUrl);

                changeMenuColor(item, getResources().getColor(R.color.notFavorite));
            }


        } else {
            Log.e("MENU UNDEFINED", "Following menu item function is not defined: " +
            item.toString());
        }
        return super.onOptionsItemSelected(item);
    }

    public void playTrailerForMovie(View view){
        //COMPLETED 2: Write code for playing trailer through an Intent from the YouTube
        if (trailerAppIntent != null && trailerWebIntent != null){
            try {
                //If app is available in the mobile
                this.startActivity(trailerAppIntent);
                Log.e("TRAILER", "Running on App");
            } catch (ActivityNotFoundException e){
                //If not available then play via web
                this.startActivity(trailerWebIntent);
                Log.e("TRAILER", "Running on Web");
            }
        }
    }

    //Utility function for changing the the Favorite Icon Color

    public void changeMenuColor(MenuItem item, int color){
        if (item != null)
            item.getIcon().setTint(color);
    }

    //Utility function for checking if the movie is already checked as favorite by user
    public boolean checkIfFavorite(SQLiteDatabase db) {
        FavoriteListDbHelper dbHelper = new FavoriteListDbHelper(this);
        db = dbHelper.getReadableDatabase();
        String [] projection = {FavoriteListEntry._ID};
        String selection = FavoriteListEntry._ID + " = ?";
        String [] selectionArgs = {String.valueOf(id)};
        Cursor queryResult = db.query(FavoriteListEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,null, null);
        Log.e("CHECK FAVORITE", "Number of rows matched: " + queryResult.getCount());
        return (queryResult.getCount() > 0);
    }

    //Background Service for fetching Trailer and Reviews
    public class MovieTrailerReviewQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wholePage.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            reviewsAdapter.notifyDataSetChanged();
            reviewsAdapter.clear();
        }

        @Override
        protected String doInBackground(URL... urls) {
            Context context = DetailsActivity.this;
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            try {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            } catch (NullPointerException e){
                Log.e("NETWORK ISSUE", "Unable to establish network connection!");
            }
            boolean isConnected = networkInfo!=null && networkInfo.isConnectedOrConnecting();
            URL trailerQuery = urls[0];
            URL reviewQuery = urls[1];
            String movieTrailerQueryResult = null;
            String movieReviewQueryResult = null;
            if (isConnected){
                try {
                    movieTrailerQueryResult = NetworkUtilities.getResponseFromHttpUrl(trailerUrl);
                    movieReviewQueryResult = NetworkUtilities.getResponseFromHttpUrl(reviewUrl);
                } catch (IOException e){
                    e.printStackTrace();
                }
                try {
                    //JSONObject for the trailer
                    JSONObject trailerJsonObject = new JSONObject(movieTrailerQueryResult);
                    //JSONObject for the Reviews
                    JSONObject reviewJsonObject = new JSONObject(movieReviewQueryResult);
                    //JSON Array for the trailers
                    JSONArray trailerResults = trailerJsonObject.getJSONArray("results");
                    //JSON Array for the reviews
                    JSONArray reviewResults = reviewJsonObject.getJSONArray("results");
                    //Fetching the first available trailer link
                    JSONObject trailer = trailerResults.getJSONObject(0);
                    String key = trailer.getString("key");
                    //Creating Intent for playing the trailer
                    trailerAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
                    trailerWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "http://www.youtube.com/watch?v=" + key));
                    ListItem item;
                    for (int counter = 0; counter < reviewResults.length(); counter++){
                        JSONObject review = reviewResults.getJSONObject(counter);
                        String comment = review.getString("content");
                        String user = review.getString("author");
                        item = new ListItem(user, comment);
                        mListData.add(item);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            return movieReviewQueryResult;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
            wholePage.setVisibility(View.VISIBLE);
            if (s != null && !s.equals("")){
                reviewsAdapter.setListData(mListData);
                mReviews.setAdapter(reviewsAdapter);
            } else {
                mListData.clear();
                reviewsAdapter.setListData(mListData);
            }
        }
    }

    //As a Guest User is not allowed to Mark a movie favorite so for a workaround
    //let the user rate it in background
    public class SetMovieAsFavorite extends AsyncTask<URL, Void, String>{

        @Override
        protected String doInBackground(URL... urls) {
            Context context = DetailsActivity.this;
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            try {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            } catch (NullPointerException e){
                Log.e("NETWORK ISSUE", "Unable to establish network connection!");
            }
            boolean isConnected = networkInfo!=null && networkInfo.isConnectedOrConnecting();
            URL favoriteUrl = urls[0];
            String postStatus = null;
            if (isConnected) {
                try {
                    postStatus = NetworkUtilities.setPostToHttpUrl(favoriteUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return postStatus;
        }
    }

    //Deleting the Rating of the movie or in foreground uncheck the movie as favorite
    public class CancelFavoriteMovie extends AsyncTask<URL, Void, String>{
        @Override
        protected String doInBackground(URL... urls) {
            Context context = DetailsActivity.this;
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            try {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            } catch (NullPointerException e){
                Log.e("NETWORK ISSUE", "Unable to establish network connection!");
            }
            boolean isConnected = networkInfo!=null && networkInfo.isConnectedOrConnecting();
            URL urlToCancelFavorite = urls[0];
            String postStatus = null;
            if (isConnected) {
                try {
                    postStatus = NetworkUtilities.requestDeleteToHttpUrl(urlToCancelFavorite);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return postStatus;
        }
    }
}
