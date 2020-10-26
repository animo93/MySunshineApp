package com.example.animo.sunshine.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.animo.sunshine.app.data.WeatherContract;
import com.example.animo.sunshine.app.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String LOG_TAG=MainActivityFragment.class.getSimpleName();
    private ForecastAdapter mforecastAdapter;
    /*private ListView mListView;
    private int mPosition=ListView.INVALID_POSITION;*/
    private RecyclerView mRecyclerView;
    //private int mPosition = RecyclerView.NO_POSITION;
    private boolean mUseTodayLayout;
    private int mChoiceMode;
    private long mInitialSelectedDate = -1;
    private boolean mAutoSelectView;


    private boolean mHoldForTransition;

    private static final String SELECTED_KEY="selected_position";

    private static final int FORECAST_LOADER=0;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;

    @Override
    public void onResume() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;

    @Override
    public void onPause() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();

        }
    }
    //private ForecastAdapter adapter;

    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }

    public MainActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }


    public void setInitialSelectedDate(long initialSelectedDate) {
        mInitialSelectedDate = initialSelectedDate;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
       /* if(mPosition!=RecyclerView.NO_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }*/
        super.onSaveInstanceState(outState);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_map) {
            openPreferredLocationInMap();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mforecastAdapter ) {
            Cursor c = mforecastAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    public void updateWeather() {

        SunshineSyncAdapter.syncImmediately(getActivity());

    }

/*    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null!=mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);
        mRecyclerView.setHasFixedSize(true);

        mforecastAdapter= new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, ForecastAdapter.ForecastAdapterViewHolder vh) {
                String locationSetting = Utility.getPreferredLocation(getActivity());
                ((Callback)getActivity())
                        .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                locationSetting,date
                        ));
                //mPosition = vh.getAdapterPosition();
            }
        } , emptyView,mChoiceMode);
//        View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);
        mRecyclerView.setAdapter(mforecastAdapter);

        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if (null != parallaxView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0) {
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
                        } else {
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                        }
                    }
                });
            }
        }

        if(savedInstanceState!=null && savedInstanceState.containsKey(SELECTED_KEY)){
            //mPosition=savedInstanceState.getInt(SELECTED_KEY);
        }
        mforecastAdapter.setUseTodayLayout(mUseTodayLayout);

        //Call the forecast API
        updateWeather();
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting=Utility.getPreferredLocation(getActivity());

        //sor order ascending by date
        String sortOrder=WeatherContract.WeatherEntry.COLUMN_DATE+" ASC";
        Uri weatherForLocationUri=WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,
                System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mforecastAdapter.swapCursor(data);
       /* if(mPosition!=ListView.INVALID_POSITION){
            mRecyclerView.smoothScrollToPosition(mPosition);
        }*/
        updateEmptyView();
        if ( data.getCount() == 0 ) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int position = mforecastAdapter.getSelectedItemPosition();
                        if (position == RecyclerView.NO_POSITION &&
                                -1 != mInitialSelectedDate) {
                            Cursor data = mforecastAdapter.getCursor();
                            int count = data.getCount();
                            int dateColumn = data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                            for ( int i = 0; i < count; i++ ) {
                                data.moveToPosition(i);
                                if ( data.getLong(dateColumn) == mInitialSelectedDate ) {
                                    position = i;
                                    break;
                                }
                            }
                        }
                        if (position == RecyclerView.NO_POSITION) position = 0;
                        // If we don't need to restart the loader, and there's a desired position to restore
                        // to, do so now.
                        mRecyclerView.smoothScrollToPosition(position);
                        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(position);
                        if (null != vh && mAutoSelectView) {
                            mforecastAdapter.selectView(vh);
                        }
                        if ( mHoldForTransition ) {
                            getActivity().supportStartPostponedEnterTransition();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

    }

    private void updateEmptyView() {
        if(mforecastAdapter.getItemCount()==0) {
            TextView textView = (TextView) getView().findViewById(R.id.recyclerview_forecast_empty);
            if(null!= textView){
                int message=R.string.empty_forecast_list;

                @SunshineSyncAdapter.LocationStatus int location= Utility.getLocationStatus(getActivity());
                switch (location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message=R.string.empty_forecast_list_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message=R.string.empty_forecast_list_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message=R.string.empty_forecast_list_invalid_location;
                        break;
                    default:
                        if(!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.empty_forecast_list_no_network;
                        }
                }
                textView.setText(message);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mforecastAdapter.swapCursor(null);

    }



    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout=useTodayLayout;
        if(mforecastAdapter!=null){
            mforecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}
