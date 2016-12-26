package com.example.animo.sunshine.app;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.animo.sunshine.app.data.WeatherContract;

/**
 * Created by animo on 19/8/16.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT=2;
    private static final int VIEW_TYPE_TODAY=0;
    private static final int VIEW_TYPE_FUTURE_DAY=1;

    private boolean mUseTodayLayout=true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public int getItemViewType(int position) {
        return (position ==0 && mUseTodayLayout) ? VIEW_TYPE_TODAY:VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
 /*   private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }*/

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
/*    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(MainActivityFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(MainActivityFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }*/

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType=getItemViewType(cursor.getPosition());
        int layoutId=-1;
        switch (viewType) {
            case VIEW_TYPE_TODAY :{
                layoutId=R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY : {
                layoutId=R.layout.list_item_forecast;
                break;
            }
        }

//        return LayoutInflater.from(context).inflate(layoutId,parent,false);
        View view=LayoutInflater.from(context).inflate(layoutId,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        /*TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));*/
//        int weatherId=cursor.getInt(MainActivityFragment.COL_WEATHER_ID);
        ViewHolder viewHolder= (ViewHolder) view.getTag();
        /*ImageView imageView= (ImageView) view.findViewById(R.id.list_item_icon);
        imageView.setImageResource(R.mipmap.ic_launcher);*/
        /*viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);*/

        int viewType=getItemViewType(cursor.getPosition());
        int weatherId= MainActivityFragment.COL_WEATHER_CONDITION_ID;
        int fallbackId=0;
        switch (viewType) {
            case VIEW_TYPE_TODAY:{
                /*viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(MainActivityFragment.COL_WEATHER_CONDITION_ID)
                )*/
                fallbackId=Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            }
            case VIEW_TYPE_FUTURE_DAY:{
                /*viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getInt(MainActivityFragment.COL_WEATHER_CONDITION_ID)
                )*/
                fallbackId=Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            }
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext,weatherId))
                .error(fallbackId)
                .crossFade()
                .into(viewHolder.iconView);

        long dateMillis=cursor.getLong(MainActivityFragment.COL_WEATHER_DATE);
        /*TextView dateView= (TextView) view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context,dateMillis));*/
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context,dateMillis));

        String description=cursor.getString(MainActivityFragment.COL_WEATHER_DESC);
       /* TextView descriptionView= (TextView) view.findViewById(R.id.list_item_forecast_textview);
        descriptionView.setText(description);*/
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast,description));

        boolean isMetric=Utility.isMetric(context);

        String high = Utility.formatTemperature(context,cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP));
        //double high=cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP);
        /*TextView highText= (TextView) view.findViewById(R.id.list_item_high_textview);
        highText.setText(Utility.formatTemperature(high,isMetric));*/
        viewHolder.highTempView.setText(high);
        viewHolder.highTempView.setContentDescription(context.getString(R.string.a11y_high_temp,high));

        String low = Utility.formatTemperature(context,cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP));
        //double low=cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP);
        /*TextView lowView= (TextView) view.findViewById(R.id.list_item_low_textview);
        lowView.setText(Utility.formatTemperature(low,isMetric));*/
        viewHolder.lowTempView.setText(low);
        viewHolder.lowTempView.setContentDescription(context.getString(R.string.a11y_low_temp,low));

    }

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout=useTodayLayout;
    }

    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView= (ImageView) view.findViewById(R.id.list_item_icon);
            dateView= (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView= (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView= (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView= (TextView) view.findViewById(R.id.list_item_low_textview);

        }

    }
}
