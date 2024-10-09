package com.example.my;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends BaseActivity {

    private ImageView logoImageView;
    private boolean isLogoOne = true; // Flag to check which logo is currently displayed
    private static final String PREFS_NAME = "LogoPrefs";
    private static final String LOGO_KEY = "current_logo";
    private static final String LOGO_STATE_KEY = "logo_state";
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_MANAGE_STORAGE = 113;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //display action bar, no up-button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Αρχική");
        }

        logoImageView = findViewById(R.id.appLogo);

        // Restore state if available
        if (savedInstanceState != null) {
            isLogoOne = savedInstanceState.getBoolean(LOGO_STATE_KEY, true); // Default to true if not set
        } else {
            // Retrieve the logo state from SharedPreferences if not available in savedInstanceState
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            isLogoOne = sharedPreferences.getBoolean(LOGO_KEY, true); // Default to true if not set
        }

        // Set the initial logo based on the stored preference
        setLogoImage();

        // Set long click listener on the logo
        logoImageView.setOnClickListener(v -> {
            swapLogos(); // Indicate that the long click event is consumed
        });


        //set up buttons and listeners
        Button pantryButton = findViewById(R.id.pantryButton);
        Button etypListButton = findViewById(R.id.etypListButton);
        Button reqListButton = findViewById(R.id.reqListButton);

        pantryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PantryActivity.class);
            startActivity(intent);
        });

        etypListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EtypListActivity.class);
            startActivity(intent);
        });

        reqListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RequiredSuppliesActivity.class);
            startActivity(intent);
        });



    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedWithStorageAccess();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void proceedWithStorageAccess() {
        // Access storage here
        Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
    }

    private void setLogoImage() {
        if (isLogoOne) {
            logoImageView.setImageResource(R.drawable.logo1);
        } else {
            logoImageView.setImageResource(R.drawable.logo2);
        }
    }

    private void swapLogos() {
        // Create a flip animation
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(logoImageView, "rotationY", 0f, 90f);
        flipOut.setDuration(300);

        // Add a listener to flip in the new logo
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Swap logos after flip out
                isLogoOne = !isLogoOne; // Toggle the logo state
                setLogoImage(); // Update the logo on the view

                // Start flip in animation
                ObjectAnimator flipIn = ObjectAnimator.ofFloat(logoImageView, "rotationY", -90f, 0f);
                flipIn.setDuration(300);
                flipIn.start();

                // Save the current logo state in SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(LOGO_KEY, isLogoOne);
                editor.apply();
            }
        });

        // Start the flip out animation
        flipOut.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the logo state in savedInstanceState
        outState.putBoolean(LOGO_STATE_KEY, isLogoOne);
    }



}
