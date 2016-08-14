package com.firefighter.skynetconfirmed;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},
                        PackageManager.PERMISSION_GRANTED);
            }

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

    }

    public void goToSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}
