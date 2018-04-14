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
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MovieList extends AppCompatActivity implements Serializable{

    private View movieFragment;
    private View progressBar;
    private ListView movieListView;
    private android.app.FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String intentExtraURL = intent.getStringExtra("first");
        String intentExtraName = intent.getStringExtra("second");
        getSupportActionBar().setTitle(intentExtraName);


        movieFragment = (View) findViewById(R.id.MovieListFragment);
        movieFragment.setVisibility(View.GONE);
        progressBar = (View) findViewById(R.id.MovieListProgressBar);

        new DownloadXmlTask().execute(intentExtraURL);
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
        StringBuilder htmlString = new StringBuilder();
        try {
            stream = downloadUrl(urlString);
            entries = feedXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        movieListView = (ListView) findViewById(R.id.MovieListView);
        final MovieListAdapter adapter = new MovieListAdapter(getApplicationContext(), entries);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                movieListView.setAdapter(adapter);
            }
        });

        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                XmlParser.Entry selectedEntry = (XmlParser.Entry) adapter.getItem(position);
                android.app.Fragment fragment = new android.app.Fragment();
                fragment = MovieDetailFragment.newInstance(selectedEntry);
                fragmentManager = getFragmentManager();
                android.app.FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.MovieListFragment, fragment).addToBackStack("movie_detail");
                ft.commit();
                movieListView.setVisibility(View.GONE);
                movieFragment.setVisibility(View.VISIBLE);
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });

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

    @Override
    public void onBackPressed(){
        if (fragmentManager.getBackStackEntryCount() > 0){
            movieFragment.setVisibility(View.GONE);
            movieListView.setVisibility(View.VISIBLE);
            fragmentManager.popBackStackImmediate();
        }
        else super.onBackPressed();

    }
}
