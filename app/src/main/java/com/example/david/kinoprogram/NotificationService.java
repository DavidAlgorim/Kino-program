package com.example.david.kinoprogram;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Work on 24.04.2018.
 */

public class NotificationService extends Service{
    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    int seconds = 30;
    private MovieList movieList = new MovieList();

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int notification_id;
    private RemoteViews remoteViews;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    public float[] distanceResults = new float[1];
    private Location currentLocation;


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        startTimer();

        return START_STICKY;
    }


    @Override
    public void onCreate(){
        Log.e(TAG, "onCreate");


    }

    @Override
    public void onDestroy(){
        Log.e(TAG, "onDestroy");
        stoptimertask();
        super.onDestroy();


    }

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();


    public  void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, seconds*1000); //
        //timer.schedule(timerTask, 5000,1000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        callNotification();
                    }
                });
            }
        };
    }

    private void callNotification(){
        getDeviceLocation();
        new DownloadXmlTask().execute("https://www.biooko.net/export/?");
        new DownloadXmlTask().execute("https://www.kinosvetozor.cz/export/?");
        new DownloadXmlTask().execute("https://www.kinoaero.cz/export/?");
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
        List<XmlParser.Entry> movieEntries = null;
        StringBuilder htmlString = new StringBuilder();
        try {
            stream = downloadUrl(urlString);
            movieEntries = feedXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        if (urlString.equals("https://www.biooko.net/export/?")){
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    50.100065, 14.430000, distanceResults);
        } else if (urlString.equals("https://www.kinoaero.cz/export/?")){
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    50.090335, 14.471891, distanceResults);
        } else if (urlString.equals("https://www.kinosvetozor.cz/export/?")){
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    50.081872, 14.430000, distanceResults);
        }

        if (distanceResults[0] <= 15000){
            for (XmlParser.Entry entry : movieEntries) {
                createNotification(entry, urlString);
            }
        }
        return htmlString.toString();
    }

    private void createNotification(XmlParser.Entry entry, String url) {
        String name = "";
        if (url.equals("https://www.biooko.net/export/?")){
            name = "Bio Oko";
        } else if (url.equals("https://www.kinoaero.cz/export/?")){
            name = "Kino Aero";
        } else if (url.equals("https://www.kinosvetozor.cz/export/?")){
            name = "Kino Světozor";
        }
        String movieDate = splitDate(entry.getStartDate());
        String movieTime = splitDate(entry.getStartDate());
        Date date = new Date();
        String todayString;
        String todayTimeString;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatDateOut = new SimpleDateFormat("dd.MM.");
        SimpleDateFormat formatTimeOut = new SimpleDateFormat("HH:mm");

        Date today = calendar.getTime();

        SimpleDateFormat formatIn = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            date = formatIn.parse(movieDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            movieDate = formatDateOut.format(date);
            movieTime = formatTimeOut.format(date);
        } catch (Exception e) {
        }

        try {
            todayString = formatDateOut.format(today);
            todayTimeString = formatTimeOut.format(today);
            String[] movieHoursMinutes = movieTime.split(":");
            String[] todayHoursMinutes = todayTimeString.split(":");
            int movieHours = (Integer.parseInt(movieHoursMinutes[0])*60) + Integer.parseInt(movieHoursMinutes[1]);
            int todayHours = (Integer.parseInt(todayHoursMinutes[0])*60) + Integer.parseInt(todayHoursMinutes[1]);
            if (movieDate.equals(todayString) /*&& (movieHours - todayHours) <= 60 && (movieHours - todayHours) > 0*/) {
                //create notification
                String[] title = entry.getTitle().split(" \\(");
                remoteViews.setTextViewText(R.id.NotificationTitle, title[0]);
                remoteViews.setTextViewText(R.id.NotificationTime, formatTimeOut.format(date));
                notification_id = (int) System.currentTimeMillis();
                Intent button_intent = new Intent("notification_button_clicked");
                button_intent.putExtra("id", notification_id);
                PendingIntent p_button_indent = PendingIntent.getBroadcast(getApplicationContext(), 123, button_intent, 0);
                remoteViews.setOnClickPendingIntent(R.id.NotificationButton, p_button_indent);

                Intent notification_intent = new Intent(getApplicationContext(), MovieList.class);
                notification_intent.putExtra("first", url);
                notification_intent.putExtra("second", name);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notification_intent, 0);
                notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setCustomContentView(remoteViews)
                        .setContentIntent(pendingIntent);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder.setSound(alarmSound);
                notificationBuilder.setVibrate(new long[]{500, 500});
                notificationManager.notify(notification_id, notificationBuilder.build());
            }
        } catch (Exception e) {
        }
    }

    public String splitDate(String textDate) {
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
        } catch (Exception e) {
        }

        return null;
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

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        currentLocation = (Location) task.getResult();


                    }else{
                        Toast.makeText(getApplicationContext(), "Nepodařilo se získat lokaci", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (SecurityException e){

        }
    }
}
