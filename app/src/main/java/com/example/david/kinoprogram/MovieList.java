package com.example.david.kinoprogram;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MovieList extends AppCompatActivity implements Serializable{

    private View movieFragment;
    private View progressBar;
    private ListView movieListView;
    private android.app.FragmentManager fragmentManager;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int notification_id;
    private RemoteViews remoteViews;
    private Context context;

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

        context = this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(),R.layout.notification);

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

        for (XmlParser.Entry entry : entries){
            createNotification(entry);
        }

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
        if (fragmentManager != null && fragmentManager.getBackStackEntryCount() > 0){
            movieFragment.setVisibility(View.GONE);
            movieListView.setVisibility(View.VISIBLE);
            fragmentManager.popBackStackImmediate();
        }
        else super.onBackPressed();

    }

    private void createNotification(XmlParser.Entry entry){
        String movieDate = splitDate(entry.getStartDate());
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
            if (movieDate.equals(todayString))
            {
                //create notification
                String[] title = entry.getTitle().split(" \\(");
                remoteViews.setTextViewText(R.id.NotificationTitle, title[0]);
                remoteViews.setTextViewText(R.id.NotificationTime, formatTimeOut.format(date));
                notification_id = (int) System.currentTimeMillis();
                Intent button_intent = new Intent("notification_button_clicked");
                button_intent.putExtra("id", notification_id);
                PendingIntent p_button_indent = PendingIntent.getBroadcast(context, 123, button_intent,0);
                remoteViews.setOnClickPendingIntent(R.id.NotificationButton,p_button_indent);

                Intent notification_intent = new Intent(context, MovieList.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,0, notification_intent, 0);
                notificationBuilder = new NotificationCompat.Builder(context);
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setCustomContentView(remoteViews)
                        .setContentIntent(pendingIntent);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder.setSound(alarmSound);
                notificationBuilder.setVibrate(new long[] { 500, 500});
                notificationManager.notify(notification_id, notificationBuilder.build());
            }
        }catch (Exception e){}
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
}
