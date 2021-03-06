package com.example.david.kinoprogram;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MovieDetailFragment extends Fragment{

    private static XmlParser.Entry movie;
    private static String cinemaName;
    private String descriptionInfo;
    private String directorString;
    private String originString;
    private String yearString;
    private String languageString;
    private String durationString;
    private View progressView;
    private View contentView;

    public static MovieDetailFragment newInstance(XmlParser.Entry entry, String name) {
        movie = entry;
        cinemaName = name;
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

        TextView directorText = (TextView)view.findViewById(R.id.MovieDetailDirectorString);
        TextView originText = (TextView)view.findViewById(R.id.MovieDetailOriginString);
        TextView yearText = (TextView)view.findViewById(R.id.MovieDetailYearString);
        TextView languageText = (TextView)view.findViewById(R.id.MovieDetailLanguageString);
        TextView durationText = (TextView)view.findViewById(R.id.MovieDetailDurationString);

        Button rezervationButton = (Button)view.findViewById(R.id.MovieDetailButton);
        rezervationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:608330088"));
                startActivity(intent);
            }
        });

        progressView = (LinearLayout)view.findViewById(R.id.MovieDetailProgressBar);
        contentView = (LinearLayout)view.findViewById(R.id.MovieDetailContent);


        if (movie != null)
        {
            if (movie.getDescription().length() > 0)
            splitDescription(movie.getDescription());
            title.setText(splitTitle(movie.getTitle()));
            date.setText(splitDate(movie.getStartDate()));
            description.setText(descriptionInfo);
            director.setText(directorString);
            origin.setText(originString);
            year.setText(yearString);
            language.setText(languageString);
            duration.setText(durationString);

            directorText.setText(R.string.director);
            originText.setText(R.string.country);
            yearText.setText(R.string.year);
            languageText.setText(R.string.language);
            durationText.setText(R.string.duration);
            progressView.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
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
        try {
            String[] splitDescriptionDirectorArray = director.split("\\) ");

            String regexYear = "(\\d{4})";
            String regexCountry = " ([A-Z \\W]){1,}[,]";
            String regexLanguage = ", \\d{1,2}:\\d{1,2}:\\d{1,2} min";

            //find year
            Pattern pattern = Pattern.compile(regexYear);
            Matcher matcher = pattern.matcher(splitDescriptionDirectorArray[1]);
            if (matcher.find()) {
                yearString = matcher.group(1);
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
        }catch (Exception e){
            noData();
        }

    }

    public String getDuration(String duration){
        if (duration.length() > 0){
            String[] splitDescriptionArray = duration.split("\n\\(");
            String[] directorArray = splitDescriptionArray[1].split(", ");
            String[] durationArray = directorArray[directorArray.length-1].split("\n\n");
            String[] splitDurationArray = durationArray[0].split(":00 min");
            String[] hoursMinutesArray = splitDurationArray[0].split(":");
            int hours = Integer.parseInt(hoursMinutesArray[0]);
            int minutes = Integer.parseInt(hoursMinutesArray[1]) + hours*60;
            duration = String.valueOf(minutes);
            return duration + " minut";
        }
        else
            return duration;
    }

    private void noData(){
        directorString = "Žádná data";
        originString = "Žádná data";
        yearString = "Žádná data";
        languageString = "Žádná data";
        durationString = "Žádná data";
    }
}
