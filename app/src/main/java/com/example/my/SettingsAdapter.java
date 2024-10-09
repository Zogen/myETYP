package com.example.my;

import static com.example.my.SettingsActivity.REQUEST_CODE_IMPORT_DB;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

    private List<SettingItem> settingItemList;
    private Context context;
    private ActivityResultLauncher<Intent> importLauncher;
    private ActivityResultLauncher<Intent> exportLauncher;
    // private DatabaseHelper databaseHelper;

    public SettingsAdapter(List<SettingItem> settingItemList, Context context, ActivityResultLauncher<Intent> importLauncher, ActivityResultLauncher<Intent> exportLauncher) {
        this.context = context; // Initialize context here
        this.importLauncher = importLauncher; // Initialize importLauncher
        this.exportLauncher = exportLauncher; // Initialize exportLauncher
        this.settingItemList = settingItemList;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingItem settingItem = settingItemList.get(position);

        // Set the setting title and icon
        holder.settingTitle.setText(settingItem.getTitle());
        holder.settingIcon.setImageResource(settingItem.getIconResId());

        // Check if this is the night mode toggle setting (let's assume it's index 0)
        if (position == 0) {
            // Make the switch visible for night mode option
            holder.nightModeSwitch.setVisibility(View.VISIBLE);

            // Set the switch state based on current night mode status
            boolean isNightModeOn = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
            holder.nightModeSwitch.setChecked(isNightModeOn);

            // Handle switch toggle for night mode
            holder.nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Toggle night mode based on switch state
                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
            });
        } else {
            // Hide the switch for other settings
            holder.nightModeSwitch.setVisibility(View.GONE);
        }

        // Handle click events
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click for each setting item
                switch (position) {
                    case 2: // Export Database
                        Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        exportIntent.setType("application/octet-stream");
                        exportIntent.putExtra(Intent.EXTRA_TITLE, "AppDatabaseBackup.db"); // Specify the file name
                        exportLauncher.launch(exportIntent); // Call exportLauncher here
                        break;
                    case 1: // Import Database // Import
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/octet-stream"); // Change as necessary
                        importLauncher.launch(intent);
                    case 3:
                        // FAQ
                        // Trigger the action to open FAQ
                        break;
                    case 4:
                        // About
                        // Trigger the action to open About section
                        break;
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return settingItemList.size();
    }

    static class SettingViewHolder extends RecyclerView.ViewHolder {
        ImageView settingIcon;
        TextView settingTitle;
        public Switch nightModeSwitch; // Add the Switch here

        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);
            settingIcon = itemView.findViewById(R.id.setting_icon);
            settingTitle = itemView.findViewById(R.id.setting_title);
            nightModeSwitch = itemView.findViewById(R.id.night_mode_switch);
        }
    }



}
