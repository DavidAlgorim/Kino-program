package com.example.david.kinoprogram;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Work on 27.03.2018.
 */

public class MovieListAdapter extends BaseAdapter {

    private Context context;
    private List<XmlParser.Entry> movie;
    private MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
    private String dayOfWeekString;
    private String timeString;
    private String dateString;
    private String slashString;

    public MovieListAdapter(Context context, List<XmlParser.Entry> movie) {
        this.context = context;
        this.movie = movie;
    }

    @Override
    public int getCount() {
        return movie.size();
    }

    @Override
    public Object getItem(int i) {
        return movie.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = View.inflate(context, R.layout.movie_list_item, null);
        TextView title = (TextView)v.findViewById(R.id.MovieListItemTitle);
        TextView time = (TextView)v.findViewById(R.id.MovieListItemTime);
        TextView date = (TextView)v.findViewById(R.id.MovieListItemDate);
        TextView day = (TextView)v.findViewById(R.id.MovieListItemDay);
        TextView slash = (TextView)v.findViewById(R.id.MovieListItemSlash);
        TextView duration = (TextView)v.findViewById(R.id.MovieListItemDuration);

        Spanned htmlAsSpanned = Html.fromHtml(movie.get(i).getDescription());
        String formatedString = htmlAsSpanned.toString();

        getDay(movieDetailFragment.splitDate(movie.get(i).getStartDate()));

        title.setText(movieDetailFragment.splitTitle(movie.get(i).getTitle()));
        time.setText(timeString);
        date.setText(dateString);
        day.setText(dayOfWeekString);
        slash.setText(slashString);
        duration.setText(movieDetailFragment.getDuration(formatedString));
        v.setTag(movie.get(i).getId());
        return v;
    }

    private void getDay(String movieDate){
        Date date = new Date();
        String todayString;
        String tomorrowString;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatDateOut = new SimpleDateFormat("dd.MM.");
        SimpleDateFormat formatTimeOut = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dayOfWeek = new SimpleDateFormat("EEEE");

        Date today = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();

        SimpleDateFormat formatIn = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            date = formatIn.parse(movieDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            movieDate = formatDateOut.format(date);
        }catch (Exception e){}

        try {
            todayString = formatDateOut.format(today);
            tomorrowString = formatDateOut.format(tomorrow);
            timeString = formatTimeOut.format(date);
            dayOfWeekString = dayOfWeek.format(date);
            dateString = movieDate;
            slashString = " / ";
            if (movieDate.equals(todayString))
            {
                dayOfWeekString = "Dnes";
                dateString = "";
                slashString = "";
            }
            else if(dateString.equals(tomorrowString))
            {
                dayOfWeekString = "Zítra";
                dateString = "";
                slashString = "";
            }
        }catch (Exception e){}
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }
}
