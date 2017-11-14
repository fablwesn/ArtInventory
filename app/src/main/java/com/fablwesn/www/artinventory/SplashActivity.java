package com.fablwesn.www.artinventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * SplashScreen displayed before the actual home activity starts {@link CatalogActivity}
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * immediately start the next activity and quits this one
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // open the home activity
        startActivity(new Intent(this, CatalogActivity.class));
        // close this activity
        finish();
    }
}
