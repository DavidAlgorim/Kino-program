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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MovieDetailFragment extends Fragment {

    private static XmlParser.Entry movie;
    private String descriptionInfo;
    private String directorString;
    private String originString;
    private String yearString;
    private String languageString;
    private String durationString;

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
        TextView director = (TextView)view.findViewById(R.id.MovieDetailDirector);
        TextView origin = (TextView)view.findViewById(R.id.MovieDetailOrigin);
        TextView year = (TextView)view.findViewById(R.id.MovieDetailYear);
        TextView language = (TextView)view.findViewById(R.id.MovieDetailLanguage);
        TextView duration = (TextView)view.findViewById(R.id.MovieDetailDuration);
        TextView description = (TextView)view.findViewById(R.id.MovieDetailDescription);


        if (movie != null)
        {
            splitDescription(movie.getDescription());
            title.setText(splitTitle(movie.getTitle()));
            date.setText(splitDate(movie.getStartDate()));
            description.setText(descriptionInfo);
            director.setText(directorString);
            origin.setText(originString);
            year.setText(yearString);
            language.setText(languageString);
            duration.setText(durationString + " minut");
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
        durationString = getDuration(formatedString);
        String[] splitDescriptionArray = formatedString.split("\n\\(");
        splitInfo(splitDescriptionArray[0]);
        splitDirector(splitDescriptionArray[1]);
    }

    private void splitInfo(String info){
        String[] splitInfoArray = info.split("\n\nPopis: ");
        descriptionInfo = splitInfoArray[1];
    }

    private void splitDirector(String director){
        String[] splitDescriptionDirectorArray = director.split("\\) ");

        String regexYear = "(\\d{4})";
        String regexCountry = " ([A-Z \\W]){1,}[,]";
        String regexLanguage = ", \\d{1,2}:\\d{1,2}:\\d{1,2} min";

        //find year
        Pattern pattern = Pattern.compile(regexYear);
        Matcher matcher = pattern.matcher(splitDescriptionDirectorArray[1]);
        if (matcher.find()) {
            yearString = matcher.group(1);  // 4 digit number
        }

        String [] splitArray = splitDescriptionDirectorArray[1].split(regexYear);

        //find directors
        String [] splitDirectorCountryArray = splitArray[0].split("Režie: ");
        String [] splitDirectorArray = splitDirectorCountryArray[1].split(regexCountry);
        directorString = splitDirectorArray[0].substring(0, splitDirectorArray[0].length()-1);

        //find country
        pattern = Pattern.compile(regexCountry);
        matcher = pattern.matcher(splitDirectorCountryArray[1]);
        if (matcher.find()) {
            originString = matcher.group();
            String [] removeOriginCommaArray = originString.split(",");
            originString = removeOriginCommaArray[0].substring(1, removeOriginCommaArray[0].length());
        }

        //find language
        String [] splitLanguageTimeArray = splitArray[1].split(regexLanguage);
        languageString = splitLanguageTimeArray[0].substring(2, splitLanguageTimeArray[0].length());
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
