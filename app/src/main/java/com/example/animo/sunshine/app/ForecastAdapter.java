package com.example.animo.sunshine.app;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
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
    public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private static final int VIEW_TYPE_COUNT=2;
    private static final int VIEW_TYPE_TODAY=0;
    private static final int VIEW_TYPE_FUTURE_DAY=1;

    final private ForecastAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    private boolean mUseTodayLayout=true;
    final private Context mContext;
    private Cursor mCursor;

    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler dh, View emptyView) {
        mContext=context;
        mClickHandler=dh;
        mEmptyView=emptyView;
    }

    public static interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ForecastAdapterViewHolder vh);
    }


    @Override
    public ForecastAdapter.ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if( viewGroup instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                }
                case VIEW_TYPE_FUTURE_DAY: {
                    layoutId = R.layout.list_item_forecast;
                    break;
                }
            }
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapter.ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        int weatherId = mCursor.getInt(MainActivityFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
        }

        if ( Utility.usingLocalGraphics(mContext) ) {
            forecastAdapterViewHolder.mIconView.setImageResource(defaultImage);
        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(forecastAdapterViewHolder.mIconView);
        }

        // Read date from cursor
        long dateInMillis = mCursor.getLong(MainActivityFragment.COL_WEATHER_DATE);

        // Find TextView and set formatted date on it
        forecastAdapterViewHolder.mDateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));

        // Read weather forecast from cursor
        String description = Utility.getStringForWeatherCondition(mContext, weatherId);

        // Find TextView and set weather forecast on it
        forecastAdapterViewHolder.mDescriptionView.setText(description);
        forecastAdapterViewHolder.mDescriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable

        // Read high temperature from cursor
        double high = mCursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP);
        String highString = Utility.formatTemperature(mContext, high);
        forecastAdapterViewHolder.mHighTempView.setText(highString);
        forecastAdapterViewHolder.mHighTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, highString));

        // Read low temperature from cursor
        double low = mCursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP);
        String lowString = Utility.formatTemperature(mContext, low);
        forecastAdapterViewHolder.mLowTempView.setText(lowString);
        forecastAdapterViewHolder.mLowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowString));


    }

    @Override
    public int getItemViewType(int position) {
        return (position ==0 && mUseTodayLayout) ? VIEW_TYPE_TODAY:VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount()==0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);

        }
    }

/*    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }*/

/*    @Override
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
    }*/

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
   /* @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder= (ViewHolder) view.getTag();

        int viewType=getItemViewType(cursor.getPosition());
        int weatherId= MainActivityFragment.COL_WEATHER_CONDITION_ID;
        int defaultImage;
        switch (getItemViewType(cursor.getPosition())) {
            case VIEW_TYPE_TODAY:{
                *//*viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(MainActivityFragment.COL_WEATHER_CONDITION_ID)
                )*//*
                //fallbackId=Utility.getArtResourceForWeatherCondition(weatherId);
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            }
           default:
               defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
        }
        if (Utility.usingLocalGraphics(mContext)) {
            viewHolder.miconView.setImageResource(defaultImage);

        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext,weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(viewHolder.miconView);
        }



        long dateMillis=cursor.getLong(MainActivityFragment.COL_WEATHER_DATE);
        *//*TextView dateView= (TextView) view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context,dateMillis));*//*
        viewHolder.mdateView.setText(Utility.getFriendlyDayString(context,dateMillis));

        String description=cursor.getString(MainActivityFragment.COL_WEATHER_DESC);
       *//* TextView descriptionView= (TextView) view.findViewById(R.id.list_item_forecast_textview);
        descriptionView.setText(description);*//*
        viewHolder.mdescriptionView.setText(description);
        viewHolder.mdescriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast,description));

        boolean isMetric=Utility.isMetric(context);

        double high = mCursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP);
        String highString = Utility.formatTemperature(mContext, high);
        viewHolder.mhighTempView.setText(highString);
        viewHolder.mhighTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, highString));



        double low = mCursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP);
        String lowString = Utility.formatTemperature(mContext, low);
        viewHolder.mlowTempView.setText(lowString);
        viewHolder.mlowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowString));

    }*/

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout=useTodayLayout;
    }
}
