package com.example.my;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ContentValues;
import android.provider.MediaStore;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    private RecyclerView settingsRecyclerView;
    SettingsAdapter settingsAdapter;
    private List<SettingItem> settingItemList;
    static final int REQUEST_CODE_IMPORT_DB = 1;
    private static final int REQUEST_CODE_READ_STORAGE = 1001;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_MANAGE_STORAGE = 113;

    ActivityResultLauncher<Intent> importLauncher;
    ActivityResultLauncher<Intent> exportLauncher;

    private DatabaseHelper databaseHelper;

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Link to the settings layout
        databaseHelper = new DatabaseHelper(this);

        // Initialize ActivityResultLaunchers
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data != null ? data.getData() : null;
                        if (uri != null) {
                            databaseHelper.importDatabase(this, uri);
                        }
                    }
                }
        );

        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Call the exportDatabase method to save the database to the selected URI
                            databaseHelper.exportDatabase(this, uri);
                        }
                    }
                }
        );

        // display action bar, with up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Ρυθμίσεις"); // Set the title to the name of the activity
        }

        settingsRecyclerView = findViewById(R.id.settings_recycler_view);
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        settingItemList = new ArrayList<>();

        // Add your settings here
        settingItemList.add(new SettingItem(R.drawable.ic_night_mode, "Night Mode"));
        settingItemList.add(new SettingItem(R.drawable.ic_import, "Import Database"));
        settingItemList.add(new SettingItem(R.drawable.ic_export, "Export Database"));
        settingItemList.add(new SettingItem(R.drawable.ic_help, "FAQ"));
        settingItemList.add(new SettingItem(R.drawable.ic_info, "About"));

        settingsAdapter = new SettingsAdapter(settingItemList, this, importLauncher, exportLauncher);
        settingsRecyclerView.setAdapter(settingsAdapter);

        // For Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Request the user to enable MANAGE_EXTERNAL_STORAGE permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
            } else {
                checkStoragePermissionAndExport();
            }
        } else {
            // For Android 6 to Android 10
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else {
                checkStoragePermissionAndExport();
            }
        }
    }

    // Function to toggle night mode
    public void toggleNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean nightModeEnabled = sharedPreferences.getBoolean(KEY_NIGHT_MODE, false);

        if (nightModeEnabled) {
            // Disable night mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            // Enable night mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Save the new state in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NIGHT_MODE, !nightModeEnabled);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_DB && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                DatabaseHelper databaseHelper = new DatabaseHelper(this);
                databaseHelper.importDatabase(this, uri);
            }
        }
    }

    private void exportDatabase() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "AppDatabaseBackup.db");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

        if (uri != null) {
            try (InputStream input = new FileInputStream(getDatabasePath("your_database_name.db")); // Change to your DB name
                 OutputStream output = getContentResolver().openOutputStream(uri)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.flush();
                Toast.makeText(this, "Database exported successfully!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error exporting database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                exportDatabase();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Call this method before exporting
    private void checkStoragePermissionAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                // Permission already granted, proceed with exporting
                exportDatabase();
            }
        } else {
            // If the Android version is less than Marshmallow, permission is granted at install time
            exportDatabase();
        }
    }

    // This method will be called when the user selects the export option
//    void exportDatabase() {
//        // Check for permissions before exporting
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        } else {
//            // If permission is already granted, proceed with exporting
//            databaseHelper.exportDatabase(this);
//        }
//    }

    private static final int REQUEST_CODE_WRITE_STORAGE = 100;

//    private void exportDatabaseWithPermissionCheck() {
//        // Check if the WRITE_EXTERNAL_STORAGE permission is granted
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            // Request the permission
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
//        } else {
//            // Permission is already granted, proceed with export
//            databaseHelper.exportDatabase(this);
//        }
//    }

    // Handle the result of the permission request
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE_WRITE_STORAGE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//                databaseHelper.exportDatabase(this);
//            } else {
//                // Permission denied
//                Toast.makeText(this, "Permission denied to write to external storage.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

}
