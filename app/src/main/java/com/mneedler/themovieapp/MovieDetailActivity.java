package com.mneedler.themovieapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;

/**
 * Created by mike on 9/24/15.
 */
public class MovieDetailActivity extends AppCompatActivity {
    private final String TAG = "TheMovieApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_detail);

        /* This movie detail Activity is called via intent.  We expect to get a Movie object and a
         * file name.  The Movie object contains most of the "detail" information we need to
         * display.  The file name is the name of a local file that contains the movie poster image
         * that also needs to be displayed.
         */
        Intent intent = this.getIntent();
        if (intent != null && intent.hasExtra("movie")) {
            Movie movie = intent.getParcelableExtra(getString(R.string.movie_key));
            String fileName = intent.getStringExtra(getString(R.string.filename_key));

            Drawable d = getImageFromFile (fileName);

            ((TextView) this.findViewById(R.id.title_TV)).setText(movie.getTitle());
            ((TextView) this.findViewById(R.id.overview_TV)).setText(movie.getOverview());
            ((TextView) this.findViewById(R.id.voteAverage_TV)).setText(Double.toString(movie.getVoteAverage()));
            ((TextView) this.findViewById(R.id.voteCount_TV)).setText(Integer.toString(movie.getVoteCount()));
            ((TextView) this.findViewById(R.id.releaseDate_TV)).setText(movie.getReleaseDate());
            ((TextView) this.findViewById(R.id.popularity_TV)).setText(movie.getPopularity());
            ((ImageView) this.findViewById(R.id.poster_imageView)).setBackground(d);
        }
    }

    /**
     * Retrieve the Drawable from an image
     * @param fileName - file that contains an image
     * @return Drawable
     */
    private Drawable getImageFromFile(String fileName) {
        File filePath = getFileStreamPath(fileName);
        return (Drawable.createFromPath(filePath.toString()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
