package com.example.david.kinoprogram;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MovieList extends AppCompatActivity implements Serializable{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String intentExtra = intent.getStringExtra("first");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        new DownloadXmlTask().execute(intentExtra);
    }


    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }
        @Override
        protected void onPostExecute(String result) {
            /*setContentView(R.layout.cinema_list);
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadData(result, "text/html", null);*/
        }
    }

    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        XmlParser feedXmlParser = new XmlParser();
        List<XmlParser.Entry> entries = null;
        String url = null;
        StringBuilder htmlString = new StringBuilder();
        try {
            stream = downloadUrl(urlString);
            entries = feedXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        final ListView movieListView = (ListView) findViewById(R.id.MovieListView);
        final MovieListAdapter adapter = new MovieListAdapter(getApplicationContext(), entries);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                movieListView.setAdapter(adapter);
            }
        });
        final List<XmlParser.Entry> entryy = entries;

        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent myIntent = new Intent(MovieList.this, MovieDetail.class);
                //List<XmlParser.Entry> dd;
                //startActivity(myIntent);
                int d = entryy.get(position).getId();

                //movieDetailFragment.onCreate(this);
                //myIntent.putExtra("movie", entries);

                android.app.Fragment fragment;
                fragment = new MovieDetailFragment();
                android.app.FragmentManager fm = getFragmentManager();
                android.app.FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.MovieListFragment, fragment);
                ft.commit();
                movieListView.setVisibility(View.GONE);

                TextView title = (TextView) findViewById(R.id.MovieDetailTitle);
                title.setVisibility(View.VISIBLE);
                title.setText(entryy.get(position).getTitle());

                TextView description = (TextView) findViewById(R.id.MovieDetailDescription);
                description.setVisibility(View.VISIBLE);
                description.setText(entryy.get(position).getDescription());

                TextView start = (TextView) findViewById(R.id.MovieDetailStart);
                start.setVisibility(View.VISIBLE);
                start.setText(entryy.get(position).getStartDate());

                TextView location = (TextView) findViewById(R.id.MovieDetailLocation);
                location.setVisibility(View.VISIBLE);
                location.setText(entryy.get(position).getLocation());

                TextView organizer = (TextView) findViewById(R.id.MovieDetailOrganizer);
                organizer.setVisibility(View.VISIBLE);
                organizer.setText(entryy.get(position).getOrganizer());

            }
        });


        for (XmlParser.Entry entry : entries) {
            htmlString.append(entry.title);
            htmlString.append(entry.description);

        }
        return htmlString.toString();
    }
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

}
