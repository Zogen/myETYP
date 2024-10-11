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
                    case 1: // Export Database
                        optionImport();
                        break;
                    case 2: // Import Database // Import
                        optionExport();
                        break;
                    case 3: // FAQ
                        // Inflate the dialog layout
                        LayoutInflater inflater = LayoutInflater.from(v.getContext());
                        View dialogView = inflater.inflate(R.layout.dialog_faq, null);

                        // Create the dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setView(dialogView);
                        AlertDialog dialog = builder.create();

                        // Set dialog content
                        TextView faqTextView = dialogView.findViewById(R.id.faq_text);
                        faqTextView.setText(Html.fromHtml(
                                "<b>Καλωσορίσατε στην Εφαρμογή Διαχείρισης ΕΤΥΠ!</b><br><br> Αυτή η εφαρμογή έχει σχεδιαστεί για να σας βοηθήσει να διαχειριστείτε αποδοτικά το ντουλάπι και τα είδη ΕΤΥΠ σας, καθιστώντας την εμπειρία των δρομολογίων σας πιο απλή και ευχάριστη. Παρακάτω παρατίθενται μερικά βασικά χαρακτηριστικά και οδηγίες για το πώς να χρησιμοποιείτε την εφαρμογή αποτελεσματικά.<br><br>" +
                                        "<b>Ξεκινώντας</b><br><br>" +
                                        "1. <b>Δημιουργία Ντουλαπιού:</b><br>" +
                                        "   - Με την εκκίνηση της εφαρμογής, θα σας ζητηθεί να δημιουργήσετε το ντουλάπι σας. Πατήστε το κουμπί 'Προσθήκη Αντικειμένου' για να ξεκινήσετε να προσθέτετε αντικείμενα που έχετε αυτή τη στιγμή στο απόθεμα σας.<br>" +
                                        "   - Εισάγετε το όνομα του αντικειμένου και καθορίστε την ποσότητα που έχετε. Μόλις προστεθούν, τα αντικείμενα σας θα εμφανιστούν στη λίστα του ντουλαπιού.<br><br>" +
                                        "2. <b>Διαχείριση Λίστας ΕΤΥΠ:</b><br>" +
                                        "   - Μεταβείτε στην ενότητα Λίστας ΕΤΥΠ για να προσθέσετε αντικείμενα που χρειάζεται να αγοράσετε. Ακριβώς όπως στο ντουλάπι, μπορείτε να προσθέσετε αντικείμενα εισάγοντας τα ονόματα και τις ποσότητές τους.<br>" +
                                        "   - Η λίστα ΕΤΥΠ θα σας βοηθήσει να παρακολουθείτε τι χρειάζεται να αγοράσετε στο επόμενο δρομολόγιο σας.<br>" +
                                        "   - Έχουμε υλοποιήσει τη δυνατότητα να μεταφέρετε μόνο τα επιλεγμένα αντικείμενα από τη λίστα σας στο ντουλάπι. Απλά επιλέξτε τα αντικείμενα που θέλετε να μεταφέρετε και πατήστε το κουμπί 'Μετακίνηση Αντικειμένων'. Αν δεν επιλέξετε κανένα αντικείμενο, θα μεταφερθούν όλα τα αντικείμενα της λίστας.<br><br>" +
                                        "3. <b>Διαχείριση Προκαθορισμένης Ποσότητας:</b><br>" +
                                        "   - Μεταβείτε στην ενότητα Απαιτούμενο Απόθεμα για να προσθέσετε αντικείμενα που θέλετε να θέσετε μια προκαθορισμένη τιμή. Η προκαθορισμένη τιμή, είναι αυτή που θα θέλαμε σε ιδανικές συνθήκες να έχουμε στο ντουλάπι.<br>" +
                                        "   - Ετσι, όταν πατήσετε την επιλογή Μεταφορά στη λίστα ΕΤΥΠ, θα καταγραφεί στη λίστα ΕΤΥΠ η υπολειπόμενη ποσότητα, την οποία την οποία αν προμηθευτούμε, θα έχουμε το ιδανικό.<br><br>" +
                                        "4. <b>Ενημέρωση Αντικειμένων:</b><br>" +
                                        "   - Μπορείτε εύκολα να ενημερώσετε τα αντικείμενα του ντουλαπιού σας. Πατήστε σε οποιοδήποτε αντικείμενο για να ανοίξετε το διάλογο επεξεργασίας, όπου μπορείτε να αλλάξετε το όνομα ή την ποσότητα του αντικειμένου.<br>" +
                                        "   - Για τα αντικείμενα ΕΤΥΠ, η διαδικασία είναι η ίδια: πατήστε για να επεξεργαστείτε.<br><br>" +
                                        "5. <b>Ρύθμιση Ποσοτήτων:</b><br>" +
                                        "   - Μπορείτε να αυξήσετε ή να μειώσετε τις ποσότητες των αντικειμένων του ντουλαπιού σας χρησιμοποιώντας τα κουμπιά '+' και '-' δίπλα σε κάθε αντικείμενο.<br>" +
                                        "   - Αν εξαντληθείτε από ένα αντικείμενο, απλά διαγράψτε το από τη λίστα του ντουλαπιού.<br><br>" +
                                        "6. <b>Εισαγωγή και Εξαγωγή Βάσης Δεδομένων:</b><br>" +
                                        "   - Μπορείτε να εξάγετε τη βάση δεδομένων σας για να δημιουργήσετε αντίγραφα ασφαλείας. Επιλέξτε την επιλογή 'Εξαγωγή Βάσης Δεδομένων' από το μενού ρυθμίσεων για να ξεκινήσετε τη διαδικασία.<br>" +
                                        "   - Για να εισάγετε μια βάση δεδομένων, επιλέξτε 'Εισαγωγή Βάσης Δεδομένων' από το μενού και επιλέξτε το αρχείο που θέλετε να εισάγετε.<br><br>" +
                                        "7. <b>Ιστορικό Συναλλαγών:</b><br>" +
                                        "   - Η εφαρμογή περιλαμβάνει μια δυνατότητα ιστορικού συναλλαγών, επιτρέποντάς σας να παρακολουθείτε τα αντικείμενα που έχετε προσθέσει ή αφαιρέσει από το ντουλάπι σας.<br>" +
                                        "   - Μπορείτε να δείτε το ιστορικό συναλλαγών μεταβαίνοντας στην ενότητα 'Ιστορικό Συναλλαγών'.<br><br>" +
                                        "<b>Πρόσθετα Χαρακτηριστικά</b><br><br>" +
                                        "- <b>Φιλικό Περιβάλλον Χρήστη:</b> Η εφαρμογή έχει σχεδιαστεί ώστε να είναι διαισθητική και φιλική προς τον χρήστη, εξασφαλίζοντας ότι μπορείτε να περιηγηθείτε μέσω του ντουλαπιού και των λιστών ΕΤΥΠ σας με ευκολία.<br>" +
                                        "<b>Συμβουλές για Καλύτερη Χρήση</b><br><br>" +
                                        "- <b>Διατηρήστε τις Λίστες σας Ενημερωμένες:</b> Ενημερώνετε τακτικά τις λίστες ντουλαπιού και ΕΤΥΠ σας ώστε να αντικατοπτρίζουν οποιεσδήποτε αλλαγές στο απόθεμά σας. Αυτό θα σας βοηθήσει να αποφύγετε διπλές αγορές.<br>" +
                                        "- <b>Εξερευνήστε την Εφαρμογή:</b> Πάρτε λίγο χρόνο για να εξερευνήσετε όλα τα χαρακτηριστικά που προσφέρει η εφαρμογή, συμπεριλαμβανομένου του ιστορικού συναλλαγών και των ρυθμίσεων χρήστη.<br>" +
                                        "- <b>Ανατροφοδότηση:</b> Εκτιμούμε την ανατροφοδότηση σας! Εάν αντιμετωπίσετε οποιοδήποτε πρόβλημα ή έχετε προτάσεις για βελτιώσεις, παρακαλούμε επικοινωνήστε μαζί μας μέσω της εφαρμογής.<br><br>" +
                                        "Ελπίζουμε να απολαύσετε τη χρήση της Εφαρμογής Διαχείρισης Ντουλαπιού & ΕΤΥΠ! Καλή οργάνωση και αγορές!"
                        ));

                        // Show the dialog
                        dialog.setCancelable(true);
                        dialog.show();
                        break;

                    case 4:
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
                        PackageManager packageManager = context.getPackageManager();
                        String packageName = context.getPackageName();

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
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
        exportIntent.setType("application/octet-stream");
        exportIntent.putExtra(Intent.EXTRA_TITLE, appLabel + " " + sdf.format(calendar.getTime()) + ".db"); // Specify the file name
        exportLauncher.launch(exportIntent); // Call exportLauncher here
    }

    public void optionImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream"); // Change as necessary
        importLauncher.launch(intent);
    }

}
