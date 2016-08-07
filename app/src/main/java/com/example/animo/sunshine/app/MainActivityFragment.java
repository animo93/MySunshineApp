package com.example.animo.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ArrayAdapter<String> adapter;

    public MainActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_refresh) {
            updateWeather();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather() {
        FetchWeatherTask fetchWeatherTask=new FetchWeatherTask();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location=preferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        fetchWeatherTask.execute(location);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_main, container, false);
        // String []forecastArray={"Today-sunny-88/63","Tomorrow-Foggy-70/46","Weds-cloudy-70/66","Thurs-rainy-64/51"};
        adapter=new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        ListView listView=(ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast=adapter.getItem(position);
                Log.e(MainActivityFragment.class.getSimpleName(),forecast);
                Intent intent=new Intent(getActivity(),DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }
    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG =FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if(strings!=null)
                adapter.clear();
            for(String s:strings)
                adapter.add(s);
        }

        private String getReadableString(long time){
            SimpleDateFormat shortenedDateFormat=new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high,double low,String unitType){
            if(unitType.equals(getString(R.string.pref_units_imperial))){
                high=(high*1.8)+32;
                low=(low*1.8)+32;
            }else if(!unitType.equals(R.string.pref_units_metric)){
                Log.e(LOG_TAG,"Unit type not found "+unitType);
            }
            long roundedHigh=Math.round(high);
            long roundedLow=Math.round(low);
            String highLowStr=roundedHigh+"/"+roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr,int numDays) throws JSONException {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson=new JSONObject(forecastJsonStr);
            JSONArray weatherArray=forecastJson.getJSONArray(OWM_LIST);

            Time dayTime=new Time();
            dayTime.setToNow();

            int julianStartDay=Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);

            dayTime=new Time();

            String[] resultStr=new String[numDays];
            SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType=preferences.getString(
                    getString(R.string.pref_units_key),
                    getString(R.string.pref_units_metric));
            for(int i=0;i<weatherArray.length();i++){
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast=weatherArray.getJSONObject(i);
                long dateTime;
                dateTime=dayTime.setJulianDay(julianStartDay + i);
                day=getReadableString(dateTime);

                JSONObject weatherObject=dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description=weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject=dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high=temperatureObject.getDouble(OWM_MAX);
                double low=temperatureObject.getDouble(OWM_MIN);

                highAndLow=formatHighLows(high,low,unitType);
                resultStr[i]=day+" - "+description+" - "+highAndLow;
            }
            return resultStr;
        }


        @Override
        protected String[] doInBackground(String[] params) {
            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
            if(params.length==0)
                return null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String format="json";
            String units="metric";
            int numDays=7;
            String appKey="e6e6e3ac01ec95be41fb344a0af6e4e8";

// Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&appid=e6e6e3ac01ec95be41fb344a0af6e4e8");
                final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";

                final String QUERY_PARAM="q";
                final String FORMAT_PARAM="mode";
                final String UNITS_PARAM="units";
                final String DAYS_PARAM="cnt";
                final String APPID_PARAM="appid";

                Uri buildUri=Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM,appKey)
                        .build();
                URL url=new URL(buildUri.toString());



                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("MainActivityFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MainActivityFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr,numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG,e.getMessage(),e);
                e.printStackTrace();
            }
            return null;
        }
    }
}
