package com.example.david.kinoprogram;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MovieDetailFragment extends Fragment {

    private static XmlParser.Entry movie;
    private String descriptionInfo;
    private String descriptionDirector;
    private String directorInfo;
    private String origin;
    private String year;
    private String version;
    private String duration;

    public static MovieDetailFragment newInstance(XmlParser.Entry entry) {
        movie = entry;
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.movie_detail, container, false);
        TextView title = (TextView)view.findViewById(R.id.MovieDetailTitle);
        TextView date = (TextView)view.findViewById(R.id.MovieDetailDate);
        TextView description = (TextView)view.findViewById(R.id.MovieDetailDescription);
        TextView director = (TextView)view.findViewById(R.id.MovieDetailDirector);

        if (movie != null)
        {
            title.setText(splitTitle(movie.getTitle()));
            date.setText(splitDate(movie.getStartDate()));
            splitDescription(movie.getDescription());
            description.setText(descriptionInfo);
            director.setText(directorInfo);
        }

        return view;
    }

    public String splitTitle(String title){
        title.split("Film:");
        String[] deleteFilm = title.split("Film: ");
        title = deleteFilm[1];
        String[] deleteDate = title.split(" \\(");
        title = deleteDate[0];

        return title;
    }

    public String splitDate(String textDate){
        Date date = new Date();
        String[] deleteZone = textDate.split("\\+");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            date = format.parse(deleteZone[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            String dateTime = dateFormat.format(date);
            return dateTime;
        }catch (Exception e){}

        return null;
    }

    private void splitDescription(String description){
        Spanned htmlAsSpanned = Html.fromHtml(description);
        String formatedString = htmlAsSpanned.toString();
        String[] splitDescriptionArray = formatedString.split("\n\\(");
        splitInfo(splitDescriptionArray[0]);
        directorInfo = splitDescriptionArray[1];
        //splitDirector(splitDescriptionArray[1]);
    }

    private void splitInfo(String info){
        String[] splitInfoArray = info.split("\n\nPopis: ");
        descriptionInfo = splitInfoArray[1];
    }

    private void splitDirector(String director){
        String[] splitDescriptionDirectorArray = director.split("\\) ");
        String [] splitArray = splitDescriptionDirectorArray[1].split(", ");
        director = splitArray[0];
        origin = splitArray[1];
        year = splitArray[2];
        version = splitArray[3];
        String [] splitDuration = splitArray[4].split("\n\n");
        duration = splitDuration[0];

    }

    public String getDuration(String duration){
        String[] splitDescriptionArray = duration.split("\n\\(");
        String[] directorArray = splitDescriptionArray[1].split(", ");
        String[] durationArray = directorArray[directorArray.length-1].split("\n\n");
        String[] splitDurationArray = durationArray[0].split(":00 min");
        String[] hoursMinutesArray = splitDurationArray[0].split(":");
        int hours = Integer.parseInt(hoursMinutesArray[0]);
        int minutes = Integer.parseInt(hoursMinutesArray[1]) + hours*60;
        duration = String.valueOf(minutes);
        return duration;
    }
}
