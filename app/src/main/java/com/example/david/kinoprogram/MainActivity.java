package com.example.david.kinoprogram;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String aeroURL =
            "https://www.kinoaero.cz/export/?";
    private static final String svetozorURL =
            "https://www.kinosvetozor.cz/export/?";
    private static final String okoURL =
            "https://www.biooko.net/export/?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.CinemaListView);

        String[] values = new String[] { "Kino Aero", "Kino Světozor", "Bio Oko" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,values);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent myIntent = new Intent(MainActivity.this, MovieList.class);
                if (i == 0){
                    myIntent.putExtra("first", aeroURL);
                    myIntent.putExtra("second", "Kino Aero");
                }
                else if(i == 1){
                    myIntent.putExtra("first", svetozorURL);
                    myIntent.putExtra("second", "Kino Světozor");
                }
                else if (i == 2){
                    myIntent.putExtra("first", okoURL);
                    myIntent.putExtra("second", "Bio Oko");
                }
                startActivity(myIntent);
            }
        });
        startService(new Intent(this, NotificationService.class));
        //new DownloadXmlTask().execute(URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
