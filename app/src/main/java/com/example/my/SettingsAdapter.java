package com.example.my;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

    private List<SettingItem> settingItemList;
    private Context context;
    private ActivityResultLauncher<Intent> importLauncher;
    private ActivityResultLauncher<Intent> exportLauncher;

    PackageManager packageManager;
    String packageName;

    public SettingsAdapter(List<SettingItem> settingItemList, Context context, ActivityResultLauncher<Intent> importLauncher, ActivityResultLauncher<Intent> exportLauncher) {
        this.context = context; // Initialize context here
        this.importLauncher = importLauncher; // Initialize importLauncher
        this.exportLauncher = exportLauncher; // Initialize exportLauncher
        this.settingItemList = settingItemList;
        this.packageName = context.getPackageName();
        this.packageManager = context.getPackageManager();
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
                    case 1: // Export Database
                        optionImport();
                        break;
                    case 2: // Import Database // Import
                        optionExport();
                        break;
                    case 3:
                        // Inflate the dialog layout
                        LayoutInflater inflater = LayoutInflater.from(v.getContext());
                        View dialogView = inflater.inflate(R.layout.dialog_new, null);

                        // Create the dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setView(dialogView);

                        try {
                            // Retrieve application info
                            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);

                            // Retrieve version information
                            String versionName = packageManager.getPackageInfo(packageName, 0).versionName;

                            builder.setTitle("v" + versionName);

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        AlertDialog dialog = builder.create();

                        // Set dialog content
                        TextView newTextView = dialogView.findViewById(R.id.new_text);
                        String newHtml = loadHtmlFromAsset("new.html");

                        if(newHtml != null){
                            newTextView.setText(Html.fromHtml(newHtml));
                        }

                        // Show the dialog
                        dialog.setCancelable(true);
                        dialog.show();
                        break;
                    case 4: // FAQ
                        // Inflate the dialog layout
                        LayoutInflater inflater1 = LayoutInflater.from(v.getContext());
                        View dialogView1 = inflater1.inflate(R.layout.dialog_faq, null);

                        // Create the dialog
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(v.getContext());
                        builder1.setView(dialogView1).setPositiveButton("OK", null);
                        AlertDialog dialog1 = builder1.create();

                        // Set dialog content
                        TextView faqTextView = dialogView1.findViewById(R.id.faq_text);

                        String faqHtml = loadHtmlFromAsset("faq.html");

                        if(faqHtml != null){
                            faqTextView.setText(Html.fromHtml(faqHtml));
                        }

                        // Show the dialog
                        dialog1.setCancelable(true);
                        dialog1.show();
                        break;
                    case 5:
                        // Inflate the dialog layout
                        LayoutInflater inflater2 = LayoutInflater.from(v.getContext());
                        View dialogView2 = inflater2.inflate(R.layout.dialog_about, null);

                        // Create the dialog
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(v.getContext());
                        builder2.setView(dialogView2);
                        AlertDialog dialog2 = builder2.create();

                        // Set dialog content
                        TextView appName = dialogView2.findViewById(R.id.app_name);
                        TextView appVersion = dialogView2.findViewById(R.id.app_version);
                        TextView appAuthor = dialogView2.findViewById(R.id.app_author);
                        TextView appLastUpdate = dialogView2.findViewById(R.id.app_last_update);

                        // Get application context and package manager
                        Context context = v.getContext();
//                        PackageManager packageManager = context.getPackageManager();
//                        String packageName = context.getPackageName();

                        try {
                            // Retrieve application info
                            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                            String appLabel = packageManager.getApplicationLabel(appInfo).toString();
                            appName.setText(appLabel); // Set app name

                            // Retrieve version information
                            String versionName = packageManager.getPackageInfo(packageName, 0).versionName;
                            int versionCode = packageManager.getPackageInfo(packageName, 0).versionCode;
                            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);

                            appVersion.setText("Version: " + versionName);
                            appAuthor.setText("Author: ΔΙΚΥΒ"); // Replace with the author's name

                            long lastUpdateTime = packageInfo.lastUpdateTime;
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String formattedDate = dateFormat.format(new Date(lastUpdateTime));
                            appLastUpdate.setText("Last Update: " + formattedDate);

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        // Show the dialog
                        dialog2.show();
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

    public void optionExport() {
        String appLabel = context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
        SimpleDateFormat sdf = new SimpleDateFormat("(yyyy-MM-dd hh:mm:ss)", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String currentTimestamp = sdf.format(calendar.getTime());

        Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
        exportIntent.setType("application/octet-stream");
        exportIntent.putExtra(Intent.EXTRA_TITLE, appLabel + currentTimestamp + ".db"); // Specify the file name
        exportLauncher.launch(exportIntent); // Call exportLauncher here
    }

    public void optionImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream"); // Change as necessary
        importLauncher.launch(intent);
    }

    // Method to load HTML content from an asset file
    private String loadHtmlFromAsset(String fileName) {
        StringBuilder htmlString = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                htmlString.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return htmlString.toString();
    }

}
