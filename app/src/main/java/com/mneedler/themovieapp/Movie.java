package com.mneedler.themovieapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mike on 9/21/15.
 */
public class Movie implements Parcelable {
    private int id;
    private String originalTitle;
    private String title;
    private String releaseDate;
    private String posterPath;
    private double voteAverage;
    private int voteCount;
    private String backdropPath;
    private String overview;
    private String popularity;


    public Movie (int id, String originalTitle, String title, String releaseDate,
                  String posterPath, double voteAverage, int voteCount, String backdropPath,
                  String overview, String popularity) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.popularity = popularity;
    }

    private Movie (Parcel in) {
        this.id = in.readInt();
        this.originalTitle = in.readString();
        this.title = in.readString();
        this.releaseDate = in.readString();
        this.posterPath = in.readString();
        this.voteAverage = in.readDouble();
        this.voteCount = in.readInt();
        this.backdropPath = in.readString();
        this.overview = in.readString();
        this.popularity = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    public String toString() {
        return  this.id + " " +
                this.originalTitle + " " +
                this.title + " " +
                this.releaseDate + " " +
                this.posterPath + " " +
                this.voteAverage + " " +
                this.voteCount + " " +
                this.backdropPath + " " +
                this.overview + " " +
                this.popularity;
    }

    @Override
    public void writeToParcel (Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.originalTitle);
        parcel.writeString(this.title);
        parcel.writeString(this.releaseDate);
        parcel.writeString(this.posterPath);
        parcel.writeDouble(this.voteAverage);
        parcel.writeInt(this.voteCount);
        parcel.writeString(this.backdropPath);
        parcel.writeString(this.overview);
        parcel.writeString(this.popularity);
    }


    public int getId() {
        return id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getOverview() {
        return overview;
    }

    public String getPopularity() {
        return popularity;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) { return new Movie(parcel); }
        @Override
        public Movie[] newArray(int i) { return new Movie[i]; }

    };
}
