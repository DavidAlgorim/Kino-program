package com.example.david.kinoprogram;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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

public class MovieList extends AppCompatActivity implements Serializable {

    private View movieFragment;
    private View progressBar;
    private ListView movieListView;
    private View listViewFragment;
    private android.app.FragmentManager fragmentManager;
    private String intentExtraURL;
    private String intentExtraName;
    private List<XmlParser.Entry> movieEntries = null;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int notification_id;
    private RemoteViews remoteViews;
    private Context context;

    private GoogleMap mMap;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private boolean mLocationPermissionsGranted = false;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    double cinemaLatitude = 0;
    double cinemaLongitude = 0;
    float[] distanceResults = new float[1];
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        intentExtraURL = intent.getStringExtra("first");
        intentExtraName = intent.getStringExtra("second");
        getSupportActionBar().setTitle(intentExtraName);

        context = this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification);

        movieFragment = (View) findViewById(R.id.MovieListFragment);
        movieFragment.setVisibility(View.GONE);
        progressBar = (View) findViewById(R.id.MovieListProgressBar);

        getLocationPermission();

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
        //entries = null;
        StringBuilder htmlString = new StringBuilder();
        try {
            stream = downloadUrl(urlString);
            movieEntries = feedXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        if (distanceResults[0] <= 1500){
            for (XmlParser.Entry entry : movieEntries) {
                createNotification(entry);
            }
        }


        movieListView = (ListView) findViewById(R.id.MovieListView);
        listViewFragment = (View) findViewById(R.id.ListViewFragment);
        final MovieListAdapter adapter = new MovieListAdapter(getApplicationContext(), movieEntries);
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
                //movieListView
                listViewFragment.setVisibility(View.GONE);
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
    public void onBackPressed() {
        if (fragmentManager != null && fragmentManager.getBackStackEntryCount() > 0) {
            movieFragment.setVisibility(View.GONE);
            listViewFragment.setVisibility(View.VISIBLE);
            fragmentManager.popBackStackImmediate();
        } else super.onBackPressed();

    }

    private void createNotification(XmlParser.Entry entry) {
        String movieDate = splitDate(entry.getStartDate());
        Date date = new Date();
        String todayString;

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
        } catch (Exception e) {
        }

        try {
            todayString = formatDateOut.format(today);
            if (movieDate.equals(todayString)) {
                //create notification
                String[] title = entry.getTitle().split(" \\(");
                remoteViews.setTextViewText(R.id.NotificationTitle, title[0]);
                remoteViews.setTextViewText(R.id.NotificationTime, formatTimeOut.format(date));
                notification_id = (int) System.currentTimeMillis();
                Intent button_intent = new Intent("notification_button_clicked");
                button_intent.putExtra("id", notification_id);
                PendingIntent p_button_indent = PendingIntent.getBroadcast(context, 123, button_intent, 0);
                remoteViews.setOnClickPendingIntent(R.id.NotificationButton, p_button_indent);

                Intent notification_intent = new Intent(context, MovieList.class);
                notification_intent.putExtra("first", intentExtraURL);
                notification_intent.putExtra("second", intentExtraName);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notification_intent, 0);
                notificationBuilder = new NotificationCompat.Builder(context);
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

    private void initilizeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (intentExtraName.equals("Kino Aero")) {
                    cinemaLatitude = 50.090335;
                    cinemaLongitude= 14.471891;
                } else if (intentExtraName.equals("Kino Světozor")) {
                    cinemaLatitude = 50.081872;
                    cinemaLongitude = 14.425264;
                } else if (intentExtraName.equals("Bio Oko")) {
                    cinemaLatitude = 50.100065;
                    cinemaLongitude = 14.430000;
                }

                MarkerOptions marker = new MarkerOptions().position(new LatLng(cinemaLatitude, cinemaLongitude)).title(intentExtraName);
                googleMap.addMarker(marker);
                if (mLocationPermissionsGranted) {
                    getDeviceLocation();
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
            }
        });
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initilizeMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION:{
                if (grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initilizeMap();
                }
            }
        }
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                    cinemaLatitude, cinemaLongitude, distanceResults);

                        }else{
                            Toast.makeText(context, "Nepodařilo se získat lokaci", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){

        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }
}
