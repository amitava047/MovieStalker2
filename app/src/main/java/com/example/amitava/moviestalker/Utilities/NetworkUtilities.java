package com.example.amitava.moviestalker.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.example.amitava.moviestalker.BuildConfig;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Amitava on 29-Nov-17.
 */

public class NetworkUtilities {

    private static final String TAG = NetworkUtilities.class.getSimpleName();
    private static final String MOVIE_DB_URL = "https://api.themoviedb.org/3/movie/";
    private static final String MOVIE_AUTH_URL = "https://api.themoviedb.org/3/authentication/";
    //For minimizing changes in the code
    private static final String MOVIE_DB_RATE = "https://api.themoviedb.org/3/";
    private static final String PARAM_SESSION = "guest_session";
    private static final String PARAM_SESSION_ID = "guest_session_id";
    private static final String PARAM_NEW_USER = "new";
    private static final String PARAM_API = "api_key";
    private static final String PARAM_RATED = "rated";
    private static final String PARAM_MOVIES = "movies";
    private static final String PARAM_RATING = "rating";
    private static final String PARAM_VIDEO = "videos";
    private static final String PARAM_REVIEW = "reviews";
    private static final String API = BuildConfig.MY_MOVIE_DB_API_KEY;


    public static URL buildUrl(String movieDbSearchQuery){
        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(movieDbSearchQuery)
                .appendQueryParameter(PARAM_API,API)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());
            Log.e("URL BUILT: ", url.toString());
        }catch (Exception e){
            //Catching Exception as line 39 can throw MalformedURLException and line 40 can throw NullPointerException
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildUrlForFavorite(String sessionKey){
        Uri uri = Uri.parse(MOVIE_DB_RATE).buildUpon()
                .appendPath(PARAM_SESSION)
                .appendPath(sessionKey)
                .appendPath(PARAM_RATED)
                .appendPath(PARAM_MOVIES)
                .appendQueryParameter(PARAM_API,API)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());
            Log.e("URL BUILT: ", url.toString());
        }catch (Exception e){
            //Catching Exception as line 39 can throw MalformedURLException and line 40 can throw NullPointerException
            e.printStackTrace();
        }
        return url;
    }

    public static  URL buildUrlForUnfavorite (int movieId, String sessionId){
        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(PARAM_RATING)
                .appendQueryParameter(PARAM_API,API)
                .appendQueryParameter(PARAM_SESSION_ID,sessionId)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());
            Log.e("URL BUILT: ", url.toString());
        }catch (Exception e){
            //Catching Exception as line 39 can throw MalformedURLException and line 40 can throw NullPointerException
            e.printStackTrace();
        }
        return url;
    }


    public static URL buildTrailerUrl (String movieId) {
        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(movieId)
                .appendPath(PARAM_VIDEO)
                .appendQueryParameter(PARAM_API, API)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
            Log.e("URL BUILT: ", url.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildReviewsUrl (String movieId) {
        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(movieId)
                .appendPath(PARAM_REVIEW)
                .appendQueryParameter(PARAM_API, API)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
            Log.e("URL BUILT: ", url.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildSessionCreationURL(){
        Uri uri = Uri.parse(MOVIE_AUTH_URL).buildUpon()
                .appendPath(PARAM_SESSION)
                .appendPath(PARAM_NEW_USER)
                .appendQueryParameter(PARAM_API,API)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e){
            Log.e("URL EXCEPTION", "URL is incorrect : " + url);
        }
        return url;
    }

    public static URL buildFavoriteMovieUrl(int movieId, String sessionId){
        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(PARAM_RATING)
                .appendQueryParameter(PARAM_API,API)
                .appendQueryParameter(PARAM_SESSION_ID,sessionId)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e){
            Log.e("URL EXCEPTION", "URL is incorrect : " + url);
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException{
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        }
        finally {
            urlConnection.disconnect();
        }
    }

    public static String setPostToHttpUrl(URL url) throws Exception{
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", 8.5);

        Log.i("JSON-POST", jsonObject.toString());
        DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
        outputStream.writeBytes(jsonObject.toString());
        outputStream.flush();
        outputStream.close();

        Log.i("POST-STATUS", String.valueOf(urlConnection.getResponseCode()));
        Log.i("POST-MSG", urlConnection.getResponseMessage());

        urlConnection.disconnect();
        return String.valueOf(urlConnection.getResponseCode());
    }

    public static JSONObject parseInputStream(InputStream inputStream){
        //read the inputStream
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        JSONObject response = new JSONObject();
        String currentLine;

        //Break it into lines of String and store it in String using StringBuilder
        try {
            while ((currentLine = reader.readLine()) != null){
                builder.append(currentLine);
            }
            //Use JSONTokener to convert the String to JsonObject
            JSONTokener jsonTokener = new JSONTokener(builder.toString());
            response = new JSONObject(jsonTokener);
        } catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }
}
