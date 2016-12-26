package com.example.animo.sunshine.app.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.animo.sunshine.app.MainActivity;
import com.example.animo.sunshine.app.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

/**
 * Created by animo on 25/12/16.
 */
public class RegistrationIntentService extends IntentService{
    private static final String TAG = "RegistrationIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {

            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                        null);
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                sendRegistrationToServer(refreshedToken);

                sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (Exception e) {
            Log.d(TAG,"Failed to complete token Refresh",e);

            sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false).apply();
        }

    }

    private void sendRegistrationToServer(String refreshedToken) {
        Log.e(TAG,"GCM Registration token "+refreshedToken);
    }
}
