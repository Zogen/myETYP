package com.example.my;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private RecyclerView settingsRecyclerView;
    SettingsAdapter settingsAdapter;
    private List<SettingItem> settingItemList;
    static final int REQUEST_CODE_IMPORT_DB = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;

    ActivityResultLauncher<Intent> importLauncher;
    ActivityResultLauncher<Intent> exportLauncher;

    private DatabaseHelper databaseHelper;

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
        settingItemList.add(new SettingItem(R.drawable.ic_new, "What's new"));
        settingItemList.add(new SettingItem(R.drawable.ic_help, "FAQ"));
        settingItemList.add(new SettingItem(R.drawable.ic_info, "About"));

        settingsAdapter = new SettingsAdapter(settingItemList, this, importLauncher, exportLauncher);
        settingsRecyclerView.setAdapter(settingsAdapter);

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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
