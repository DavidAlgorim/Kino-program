package com.example.david.kinoprogram;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MovieDetailFragment extends Fragment {
    private static XmlParser.Entry movie;

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

        if (movie != null)
        {
            title.setText(splitTitle(movie.getTitle()));
            date.setText(splitDate(movie.getStartDate()));
            description.setText(movie.getDescription());
        }

        return view;
    }

    private String splitTitle(String title){

        title.split("Film:");
        String[] deleteFilm = title.split("Film: ");
        title = deleteFilm[1];
        String[] deleteDate = title.split(" \\(");
        title = deleteDate[0];

        return title;
    }

    private String splitDate(String textDate){
        Date date = new Date();
        String[] deleteZone = textDate.split("\\+");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            date = format.parse(deleteZone[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            String dateTime = dateFormat.format(date);
            return dateTime;
        }catch (Exception e){}

        return null;
    }
}
