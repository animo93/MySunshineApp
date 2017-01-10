package com.example.animo.sunshine.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by animo on 21/12/16.
 */

public class LocationEditTextPreference extends EditTextPreference {
    private int minLength;
    static final private int DEFAULT_MINIMUM_LOCATION_LENGTH =2;

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We'll use a toast for now so that we can test our new preference com.example.animo.sunshine.app.widget.
                //Toast.makeText(getContext(), "Woo!", Toast.LENGTH_LONG).show();
                Context context = getContext();

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                Activity settingsActivity = (Activity) context;
                try{
                    settingsActivity.startActivityForResult(builder.build(settingsActivity),SettingsActivity.PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {

                }

            }
        });

        return view;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        EditText editText= getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = getDialog();
                if( dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if(s.length()<minLength){
                        positiveButton.setEnabled(false);
                    } else {
                        positiveButton.setEnabled(true);
                    }
                }

            }
        });
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs) {

        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0,0);
        try {
            minLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if(resultCode == ConnectionResult.SUCCESS) {
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }
}
