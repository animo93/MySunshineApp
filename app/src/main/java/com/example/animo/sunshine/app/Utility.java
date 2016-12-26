package com.example.animo.sunshine.app;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;

import com.example.animo.sunshine.app.sync.SunshineSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by animo on 19/8/16.
 */
public class Utility {

    public static final String DATE_FORMAT="yyyMMdd";

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context,double temperature) {
        String suffix="\u00B0";
        if(!isMetric(context)) {
            temperature=(temperature*1.8)+32;
        }
        return String.format(context.getString(R.string.format_temperature),temperature);
    }

    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    @SuppressLint("StringFormatMatches")
    public static String getFriendlyDayString(Context context, long dateInMillis){
        Time time=new Time();
        time.setToNow();
        long currentTime=System.currentTimeMillis();
        int julianDay=Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay=Time.getJulianDay(currentTime,time.gmtoff);

        if(julianDay==currentJulianDay){
            String today=context.getString(R.string.today);
            int formatId=R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context,dateInMillis)));
        } else if (julianDay < currentJulianDay + 7){
            return getDayName(context,dateInMillis);
        } else {
            SimpleDateFormat shortendDateFormat=new SimpleDateFormat("EEE MMM dd");
            return shortendDateFormat.format(dateInMillis);
        }
    }

    public static Object getFormattedMonthDay(Context context, long dateInMillis) {
        Time time=new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat=new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat=new SimpleDateFormat("MMMM dd");
        String monthDayString =monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getDayName(Context context, long dateInMillis) {
        Time t=new Time();
        t.setToNow();
        int julianDay=Time.getJulianDay(dateInMillis,t.gmtoff);
        int currentJulianDay=Time.getJulianDay(System.currentTimeMillis(),t.gmtoff);
        if(julianDay==currentJulianDay) {
            return context.getString(R.string.today);
        } else if(julianDay==currentJulianDay+1){
            return context.getString(R.string.tomorrow);
        } else {
            Time time=new Time();
            time.setToNow();
            SimpleDateFormat dayFormat=new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }
    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }
    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = conn.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("WrongConstant")
    static public @SunshineSyncAdapter.LocationStatus
    int getLocationStatus(Context c) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPreferences.getInt(c.getString(R.string.pref_location_status_key   ),SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    public static void resetLocationStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe= sp.edit();
        spe.putInt(c.getString(R.string.pref_location_status_key),SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
        spe.apply();
    }

    @SuppressLint("StringFormatInvalid")
    public static String getArtUrlForWeatherCondition(Context context, int weatherId){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String formatArtUrl = preferences.getString(context.getString(R.string.pref_art_pack_key),
                context.getString(R.string.pref_art_pack_sunshine));
        if (weatherId >= 200 && weatherId <= 232) {
            return context.getString(R.string.format_art_url,"storm");
        } else if (weatherId >= 300 && weatherId <= 321) {
            return context.getString(R.string.format_art_url,"light_rain");
        } else if (weatherId >= 500 && weatherId <= 504) {
            return context.getString(R.string.format_art_url,"rain");
        } else if (weatherId == 511) {
            return context.getString(R.string.format_art_url,"snow");
        } else if (weatherId >= 520 && weatherId <= 531) {
            return context.getString(R.string.format_art_url,"rain");
        } else if (weatherId >= 600 && weatherId <= 622) {
            return context.getString(R.string.format_art_url,"snow");
        } else if (weatherId >= 701 && weatherId <= 761) {
            return context.getString(R.string.format_art_url,"fog");
        } else if (weatherId == 761 || weatherId == 781) {
            return context.getString(R.string.format_art_url,"storm");
        } else if (weatherId == 800) {
            return context.getString(R.string.format_art_url,"clear");
        } else if (weatherId == 801) {
            return context.getString(R.string.format_art_url,"light_clouds");
        } else if (weatherId >= 802 && weatherId <= 804) {
            return context.getString(R.string.format_art_url,"clouds");
        }
        return null;
    }
}
