package com.example.david.kinoprogram;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Work on 27.03.2018.
 */

public class MovieListAdapter extends BaseAdapter {

    private Context context;
    private List<XmlParser.Entry> movie;

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

        title.setText(movie.get(i).getTitle());
        time.setText(movie.get(i).getStartDate());
        v.setTag(movie.get(i).getId());
        return v;
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }
}
