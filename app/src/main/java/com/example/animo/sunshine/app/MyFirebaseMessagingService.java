package com.example.animo.sunshine.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by animo on 22/12/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";

    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_WEATHER = "weather";
    private static final String EXTRA_LOCATION = "location";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (!remoteMessage.getData().isEmpty()) {
            String senderId = getString(R.string.gcm_defaultSenderId);
            if(senderId.length() ==0 ) {
                Toast.makeText(this,
                        "Sender Id string needs to be set",
                        Toast.LENGTH_LONG).show();
            }

            if (senderId.equals(remoteMessage.getFrom())) {
                try {
                    JSONObject jsonObject = new JSONObject(remoteMessage.getData().get(EXTRA_DATA));
                    String weather = jsonObject.getString(EXTRA_WEATHER);
                    String location = jsonObject.getString(EXTRA_LOCATION);
                    String alert = String.format(getString(R.string.gcm_weather_alert),
                            weather,
                            location);
                    sendNotification(alert);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG,"Received: " + remoteMessage.getData().toString());
        }
    }

    private void sendNotification(String alert) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent =
                PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);

        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(),R.drawable.art_storm);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.art_clear)
                .setLargeIcon(largeIcon)
                .setContentTitle("Weather Alert ")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(alert))
                .setContentText(alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID,mBuilder.build());
    }
}
