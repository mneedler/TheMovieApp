package com.mneedler.themovieapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TheMovieAppMainActivity extends AppCompatActivity {
    private final String TAG = "TheMovieApp";
    private final String API_KEY = "--your key from themoviedb.org goes here--";
    private final String IMAGE_FILENAME = "detail_image.png";

    private GridViewAdapter mGridAdapter;
    private ArrayList<Movie> mMovieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            mMovieData = new ArrayList<>();
            updateMovies();
        }
        else {
            mMovieData = savedInstanceState.getParcelableArrayList("movies");
        }

        setContentView(R.layout.activity_the_movie_app_main);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        mGridAdapter = new GridViewAdapter(this, mMovieData);
        gridView.setAdapter(mGridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMovieData.get(position);
                ImageView iv = (ImageView) view;
                writeImageToFile(IMAGE_FILENAME, iv);

                //Log.v(TAG, "onItemClick(): position = " + position + "  " + movie.getTitle());
                Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);
                Bundle extras = new Bundle();
                extras.putParcelable(getString(R.string.movie_key), movie);
                extras.putString(getString(R.string.filename_key), IMAGE_FILENAME);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putParcelableArrayList("movies", mMovieData);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_the_movie_app_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                FetchMoviesTask fmt = new FetchMoviesTask();
                fmt.execute("popularity.desc");
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                Log.e(TAG, "Unknown menu command");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO: I don't think we need to updateMovies() unless the sort order preference changed
        updateMovies();
    }

    /**
     * writeImageToFile
     * Retrieves the bitmap image within an ImageView and writes it to a (png) file.
     *
     * This appears to work pretty well on the phone, however on the emulator it generates
     * (graphics card?) errors and after about 3 calls corrupts the emulator screen with
     * random chunks of images making it unreadable and unuseable.
     *
     * @param fileName - where the image will be written to
     * @param view - where the image will be taken from
     */
    private void writeImageToFile (String fileName, ImageView view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = view.getDrawingCache(true);

        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
    }

    private void updateMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrder = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_highest_rated));

        FetchMoviesTask fmt = new FetchMoviesTask();
        fmt.execute(sortOrder);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0; // default is fail

            // If there's no sort_by, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return result;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;


            try {
                // build the command (url)
                final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])
                        .appendQueryParameter(KEY_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                //Log.v(TAG, "Built URI " + builtUri.toString());

                // send the command
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // get the response to our command
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return result;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                // parse the response
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return result;
                }
                movieJsonStr = buffer.toString();
                //Log.v(TAG, "Movie  string: " + movieJsonStr);

            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return result;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getMovieDataFromJson(movieJsonStr);
                result = 1;  // success
            }
            catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Lets update UI
            //Log.v(TAG, "onPostExecute(): result = " + result);

            if (result == 1) {
                mGridAdapter.setGridData(mMovieData);
            } else {
                Toast.makeText(TheMovieAppMainActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

            return;
        }

        private ArrayList<Movie> getMovieDataFromJson (String JsonStr) throws JSONException {
            mMovieData = new ArrayList<Movie>();

            final String MOVIE_ID       = "id";
            final String RESULTS        = "results";
            final String BACKDROP_PATH  = "backdrop_path";
            final String ORIGINAL_TITLE = "original_title";
            final String OVERVIEW       = "overview";
            final String RELEASE_DATE   = "release_date";
            final String POSTER_PATH    = "poster_path";
            final String POPULARITY     = "popularity";
            final String TITLE          = "title";
            final String VOTE_AVERAGE   = "vote_average";
            final String VOTE_COUNT     = "vote_count";

            JSONObject movieJson = new JSONObject(JsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS);

            //Log.v(TAG, "Number of movies we got back is " + movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject jObject = movieArray.getJSONObject(i);

                int id               = jObject.getInt(MOVIE_ID);
                String title         = jObject.getString(TITLE);
                String originalTitle = jObject.getString(ORIGINAL_TITLE);
                String backdropPath  = jObject.getString(BACKDROP_PATH);
                String overview      = jObject.getString(OVERVIEW);
                String releaseDate   = jObject.getString(RELEASE_DATE);
                String posterPath    = jObject.getString(POSTER_PATH);
                String popularity    = jObject.getString(POPULARITY);
                double voteAverage   = jObject.getDouble(VOTE_AVERAGE);
                int voteCount        = jObject.getInt(VOTE_COUNT);

                //Log.v(TAG, "----------------------------");
                //Log.v(TAG, "id: " + id);
                //Log.v(TAG, "title: " + title);
                //Log.v(TAG, "original title: " + originalTitle);
                //Log.v(TAG, "backdrop path: " + backdropPath);
                //Log.v(TAG, "release date: " + releaseDate);
                //Log.v(TAG, "poster path: " + posterPath);
                //Log.v(TAG, "popularity: " + popularity);
                //Log.v(TAG, "vote average: " + voteAverage);
                //Log.v(TAG, "vote count: " + voteCount);
                //Log.v(TAG, "overview: " + overview);

                if (title == null || title == "null") {
                    if (originalTitle == null || originalTitle == "null") {
                        title = "Unknown";
                    }
                    else {
                        title = originalTitle;
                    }
                }

                if (originalTitle == null || originalTitle == "null") originalTitle = "Unknown";
                if (posterPath == null || posterPath == "null") posterPath = backdropPath;
                if (releaseDate == null || releaseDate == "null") releaseDate = "unknown";
                if (popularity == null || popularity == "null") popularity = "unknown";
                if (overview == null || overview == "null") overview = "Synopsis is unavailable.";

                mMovieData.add(new Movie(id, originalTitle, title, releaseDate, posterPath,
                        voteAverage, voteCount, backdropPath, overview, popularity));
            }

            return mMovieData;
        }
    }
}
