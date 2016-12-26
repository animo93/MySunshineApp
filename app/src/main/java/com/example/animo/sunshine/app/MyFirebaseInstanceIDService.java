package com.example.animo.sunshine.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.animo.sunshine.app.gcm.RegistrationIntentService;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by animo on 22/12/16.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseInstanceIDService";
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Intent intent = new Intent(this,RegistrationIntentService.class);
        startService(intent);
    }
}
