package com.example.amitava.moviestalker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.amitava.moviestalker.Data.FavoriteListContract.FavoriteListEntry;
import com.example.amitava.moviestalker.Data.FavoriteListContract;
import com.example.amitava.moviestalker.Data.FavoriteListDbHelper;
import com.example.amitava.moviestalker.Utilities.GridItem;
import com.example.amitava.moviestalker.Utilities.NetworkUtilities;
import com.example.amitava.moviestalker.Utilities.PosterAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    GridView mMovieList;
    PosterAdapter mGridAdapter;
    Spinner mSortBy;
    TextView mErrorMsg;
    ProgressBar mProgressBar;
    ArrayList<GridItem> mGridData;
    private String sessionId = null;

    private SQLiteDatabase mDb;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovieList = findViewById(R.id.gv_movie_list);
        //Initialize the GridView with no data
        mGridData = new ArrayList<>();
        mGridAdapter = new PosterAdapter(this, R.layout.grid_item_layout, mGridData);
        mMovieList.setAdapter(mGridAdapter);

        //Initiate the Spinner and fill it with data and defining the tasks when item is selected
        mSortBy = (Spinner) findViewById(R.id.sp_sort_by);
        ArrayAdapter<CharSequence> sortByAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by_option,
                R.layout.support_simple_spinner_dropdown_item);
        sortByAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSortBy.setAdapter(sortByAdapter);
        mSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if(item!=null){
                    changeMovieSort(item.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mErrorMsg = (TextView) findViewById(R.id.tv_error_msg_disp);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mMovieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                showToast("Pos: "+ position +" id:"+id);
                GridItem item = (GridItem) adapterView.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());
                intent.putExtra("release_date", item.getReleaseDate());
                intent.putExtra("rating", item.getRating());
                intent.putExtra("synopsis", item.getOverview());
                intent.putExtra("id", item.getId());
                intent.putExtra("session_id", sessionId);

                startActivity(intent);
            }
        });

        //Create a SharedPreference to store the Device Session ID or if already stored then fetch it
        sharedPreferences = getBaseContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(getString(R.string.preference_session_key))){
            URL sessionUrl = NetworkUtilities.buildSessionCreationURL();
            new CreateSession().execute(sessionUrl);
        } else {
            sessionId = sharedPreferences.getString(getString(R.string.preference_session_key),null);
        }
    }

    private void changeMovieSort(String chosenOption){
        String temp;
        URL movieDbSearchUrl;
        mGridData.clear();
        if(chosenOption.equals(getResources().getStringArray(R.array.sort_by_option)[0])){
            temp = "popular";
            Log.e("POPULAR", "Movies are sorted by popularity with Session: " + sessionId);
            movieDbSearchUrl = NetworkUtilities.buildUrl(temp);
            new MovieDbQueryTask().execute(movieDbSearchUrl);
        }
        if (chosenOption.equals(getResources().getStringArray(R.array.sort_by_option)[1])){
            temp = "top_rated";
            Log.e("RATING", "Movies are sorted by rating");
            movieDbSearchUrl = NetworkUtilities.buildUrl(temp);
            new MovieDbQueryTask().execute(movieDbSearchUrl);
        }
        if (chosenOption.equals(getResources().getStringArray(R.array.sort_by_option)[2])){
            temp = sessionId;
            Log.e("FAVORITE", "Movies are sorted as User Favorite with Session: " + temp);
            movieDbSearchUrl = NetworkUtilities.buildUrlForFavorite(temp);
            new MovieDbQueryTask().execute(movieDbSearchUrl);
        }
    }

    public void showJsonMessage() {
        mErrorMsg.setVisibility(View.INVISIBLE);
//        mTesting.setVisibility(View.VISIBLE);
    }

    public void showErrorMessage() {
//        mTesting.setVisibility(View.INVISIBLE);
        mErrorMsg.setVisibility(View.VISIBLE);
    }

    private class MovieDbQueryTask extends AsyncTask<URL, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mTesting.setVisibility(View.INVISIBLE);
            mErrorMsg.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            //ensuring the page refreshes after option change
            mGridAdapter.notifyDataSetChanged();
            mGridAdapter.clear();
        }

        @Override
        protected String doInBackground(URL... urls) {
            Context context = MainActivity.this;
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            URL queryUrl = urls[0];
            String movieDbQueryResult = null;
            if(isConnected){
                try{
                    movieDbQueryResult = NetworkUtilities.getResponseFromHttpUrl(queryUrl);
                } catch (IOException e){
                    e.printStackTrace();
                }
                try{
                    //Store the JSON Object
                    JSONObject jsonObject = new JSONObject(movieDbQueryResult);
                    //Retrieve the JSON Array
                    JSONArray movieResults = jsonObject.getJSONArray("results");
                    //GridItem Object to store the values for each item
                    GridItem item;
                    //Loop for retrieving all the Posters ID and storing in the view
                    for(int counter=0; counter<movieResults.length(); counter++){
                        JSONObject movie = movieResults.getJSONObject(counter);
                        int id = movie.getInt("id");
                        String posterPath = movie.getString("poster_path");
                        String posterTitle = movie.getString("title");
                        String posterDetails = movie.getString("overview");
                        String posterReleaseDate = movie.getString("release_date");
                        String posterRating = movie.getString("vote_average");
                        item = new GridItem();
                        item.setId(id);
                        item.setTitle(posterTitle);
                        item.setImage("http://image.tmdb.org/t/p/w500/" +posterPath);
                        item.setOverview(posterDetails);
                        item.setRating(posterRating);
                        item.setReleaseDate(posterReleaseDate);
                        mGridData.add(item);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            return movieDbQueryResult;
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if(s!=null && !s.equals("")){
                showJsonMessage();
                mGridAdapter.setGridData(mGridData);
                mMovieList.setAdapter(mGridAdapter);
            }else {
                showErrorMessage();
            }
        }
    }

    private class CreateSession extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            Context context = MainActivity.this;
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            String response = null;
            String session = null;
            URL queryUrl = urls[0];
            if(isConnected) {
                try {
                    response = NetworkUtilities.getResponseFromHttpUrl(queryUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    session = jsonObject.getString("guest_session_id");
                } catch (JSONException j){
                    Log.e("JSON ERROR", response);
                }
            }
            sessionId = session;
            return session;
        }

        @Override
        protected void onPostExecute(String s) {
            sharedPreferences.edit().putString(getString(R.string.preference_session_key), sessionId).apply();
        }
    }
}
