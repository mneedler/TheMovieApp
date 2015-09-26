package com.mneedler.themovieapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

/**
 * Created by mike on 9/22/15.
 */
public class GridViewAdapter extends BaseAdapter {
    private final String TAG = "TheMovieApp";
    private final Context mContext;
    private ArrayList<Movie> mGridData = new ArrayList<Movie>();

    public GridViewAdapter(Context context, ArrayList<Movie> mGridData) {
        this.mContext = context;
        this.mGridData = mGridData;
    }

    /**
     * Updates grid data and refresh grid items.
     *
     * @param movieArray
     */
    public void setGridData(ArrayList<Movie> movieArray) {
        mGridData.clear();
        mGridData.addAll(movieArray);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view;

        if (convertView == null) {
            view = new ImageView(mContext);
            view.setPadding(2,2,2,2);
        }
        else {
            view = (ImageView) convertView;
        }

        Movie movie = mGridData.get(position);

        // If we don't have a movie poster path then we display a generic
        // "movie poster not available" image.  If we do have a poster path then we need to
        // get the poster image from themoviedb.
        if (movie.getPosterPath() == null || movie.getPosterPath() == "null") {
            view.setImageResource(R.drawable.movie_unavailable);
        }
        else {
            final String MOVIE_DB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
            final String IMAGE_SIZE = "w500";
            String posterImagePath = MOVIE_DB_IMAGE_BASE_URL + IMAGE_SIZE + movie.getPosterPath();
            Picasso.with(mContext).load(posterImagePath).into(view);
        }

        return view;
    }

    @Override public int getCount() {
        return mGridData.size();
    }

    @Override public String getItem(int position) {
        Movie movie = mGridData.get(position);
        return movie.getPosterPath();
    }

    @Override public long getItemId(int position) {
        return position;
    }
}
