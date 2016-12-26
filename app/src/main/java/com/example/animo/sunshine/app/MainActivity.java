package com.example.animo.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import com.example.animo.sunshine.app.gcm.RegistrationIntentService;
import com.example.animo.sunshine.app.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback{
    public static final  String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private final String LOG_TAG=MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLocation=Utility.getPreferredLocation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = java.lang.String(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        Log.e(MainActivity.class.getSimpleName(), "mTwoPane start");
        if(findViewById(R.id.weather_detail_container)!=null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }else {
            mTwoPane=false;
            getSupportActionBar().setElevation(9f);
        }
        Log.e(MainActivity.class.getSimpleName(),"mTwoPane "+mTwoPane);

        MainActivityFragment mainActivityFragment= (MainActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        mainActivityFragment.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);

        if(checkPlayServices()){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER,false);
            if(!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
        Log.e(MainActivity.class.getSimpleName(),"mTwoPane end");
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this,
                        resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG,"This device is not supported");
                finish();
            }
            return false;
        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location=Utility.getPreferredLocation(this);
        if(location!=null && !location.equals(mLocation)){
            MainActivityFragment ff= (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if(null!=ff){
                ff.onLocationChanged();
            }
            DetailActivityFragment df= (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if(null!=df){
                df.onLocationChanged(location);
            }
            mLocation=location;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }
        if (id==R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String location=preferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        Uri geoLocation=Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if(intent.resolveActivity(getPackageManager())!=null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG,"Couldn't Call "+location+" ,no receiving apps installed");
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if(mTwoPane){
            Bundle args=new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI,dateUri);

            DetailActivityFragment fragment=new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container,fragment,DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent=new Intent(this,DetailActivity.class).setData(dateUri);
            startActivity(intent);
        }

    }
}
