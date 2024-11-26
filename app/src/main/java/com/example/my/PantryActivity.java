package com.example.my;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PantryActivity extends BaseActivity implements SearchView.OnQueryTextListener{

    private DatabaseHelper dbHelper;
    private RecyclerView pantryRecyclerView;
    private PantryAdapter adapter;
    private List<PantryItem> pantryList;
    private static final String SORT_PREFERENCE_KEY = "SortPreference_Pantry";
    private ArrayAdapter<String> autoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());

        // display action bar with up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Ντουλάπι ΔΙΚΥΒ"); // Set the title to the name of the activity
            actionBar.setDisplayHomeAsUpEnabled(true); // Enable the Up button
        }

        //initializing activity components
        dbHelper = new DatabaseHelper(this);
        pantryRecyclerView = findViewById(R.id.pantryRecyclerView);
        pantryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pantryList = new ArrayList<>();
        adapter = new PantryAdapter(this, pantryList, dbHelper); // Pass 'this' as the listener
        pantryRecyclerView.setAdapter(adapter);

        //method to load items registered in Pantry db table, implemented below
        loadPantryItems();
        loadSortPreference();

        //button to add new item to pantry
        Button addPantryItemButton = findViewById(R.id.addPantryItemButton);
        addPantryItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPantryItemDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pantry, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Make the SearchView expand to the full width
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Set up listener to handle search text changes
        searchView.setOnQueryTextListener(this);

        // Listen for search view close to reload entire list
        searchView.setOnCloseListener(() -> {
            loadPantryItems();  // Reload entire list when search is closed
            return false;
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortOptionsDialog(); // Show the sorting options dialog
            return true; // Indicate the event has been handled
        }
        return super.onOptionsItemSelected(item); // Delegate unhandled events to the superclass
    }

    private void showSortOptionsDialog() {
        String[] sortOptions = {"Ημερομηνία εισαγωγής", "Ημερομηνία (αντιστρ.)", "Αλφαβητικά", "Αλφαβητικά (αντιστρ.)"};

        // Create a dialog with radio buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ταξινόμηση με βάση:");

        builder.setSingleChoiceItems(sortOptions, getCurrentSortOptionIndex(), (dialog, which) -> {
            // Handle selection
            switch (which) {
                case 0:
                    sortListByUniqueId(false);
                    setCurrentSortOption(0);
                    break;
                case 1:
                    sortListByUniqueId(true);
                    setCurrentSortOption(1);
                    break;
                case 2:
                    sortListAlphabetically(true);
                    setCurrentSortOption(2);
                    break;
                case 3:
                    sortListAlphabetically(false);
                    setCurrentSortOption(3);
                    break;
            }

            setCurrentSortOption(which); // Save the selected option
            dialog.dismiss(); // Close the dialog
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sortListByUniqueId(boolean reverse) {
        // Assume the items have a method `getId()` to retrieve the unique ID
        if (reverse) {
            Collections.sort(pantryList, (item1, item2) -> Integer.compare(item2.getId(), item1.getId()));
        } else {
            Collections.sort(pantryList, (item1, item2) -> Integer.compare(item1.getId(), item2.getId()));
        }
        adapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
    }

    private void sortListAlphabetically(boolean ascending) {
        if (ascending) {
            Collections.sort(pantryList, (item1, item2) -> item1.getName().compareToIgnoreCase(item2.getName()));
        } else {
            Collections.sort(pantryList, (item1, item2) -> item2.getName().compareToIgnoreCase(item1.getName()));
        }
        adapter.notifyDataSetChanged();
    }

    private int currentSortOption = 0; // Default: Auto Increment

    private int getCurrentSortOptionIndex() {
        return currentSortOption;
    }

    private void setCurrentSortOption(int index) {
        currentSortOption = index;

        // Save to SharedPreferences
        SharedPreferences preferences = getSharedPreferences("SortPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SORT_PREFERENCE_KEY, index);
        editor.apply();
    }

    @Override
    public boolean onQueryTextSubmit(String query){
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText){
        newText = newText.toLowerCase();
        ArrayList<PantryItem> newList = new ArrayList<>();
        for (PantryItem item : pantryList)
        {
            String name = item.getName().toLowerCase();
            if (name.contains(newText)){
                newList.add(item);
            }
        }
        adapter.setFilter(newList);
        return  true;
    }

    private void loadPantryItems() {
        Cursor cursor = dbHelper.getAllPantryItems();
        pantryList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                pantryList.add(new PantryItem(id, name, quantity));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddPantryItemDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null); // Use your XML layout

        // Get references to the input fields
        AutoCompleteTextView itemNameInput = dialogView.findViewById(R.id.item_name_input);
        EditText itemQuantityInput = dialogView.findViewById(R.id.item_quantity_input);

        // Prepare suggestions based on the current etyp list
        List<String> itemNames = new ArrayList<>();
        for (PantryItem item : pantryList) {
            itemNames.add(item.getName());
        }

        // Set up the AutoCompleteTextView with suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, itemNames);
        itemNameInput.setAdapter(adapter);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Προσθήκη στο ντουλάπι ΔΙΚΥΒ");
        builder.setView(dialogView);

        builder.setPositiveButton("Προσθήκη", (dialog, which) -> {
            String itemName = itemNameInput.getText().toString().trim();
            String itemQuantityStr = itemQuantityInput.getText().toString().trim();

            // Check that both fields have been filled and that quantity is > 0
            if (!itemName.isEmpty() && !itemQuantityStr.isEmpty()) {
                int itemQuantity = Integer.parseInt(itemQuantityStr);

                // Find similar items
                PantryItem similarItem = findSimilarItem(pantryList, itemName);

                if (similarItem != null) {
                    // Prompt user to confirm if they want to update the similar item
                    new AlertDialog.Builder(this)
                            .setTitle("Βρέθηκε παρόμοιο στοιχείο")
                            .setMessage("Ποιό απ' τα δύο να προστεθεί στη λίστα;")
                            .setPositiveButton(similarItem.getName(), (innerDialog, innerWhich) -> {
                                // Update the existing item
                                int newQuantity = similarItem.getQuantity() + itemQuantity;
                                dbHelper.updatePantryItemQuantity(similarItem.getId(), newQuantity);
                                Toast.makeText(this, "Η ποσότητα ενημερώθηκε.", Toast.LENGTH_SHORT).show();
                                updateSuggestions();
                                loadPantryItems();
                                loadSortPreference();
                            })
                            .setNegativeButton(itemName, (innerDialog, innerWhich) -> {
                                // Add a new item
                                dbHelper.insertPantryItem(itemName, itemQuantity);
                                Toast.makeText(this, "Προστέθηκε νέο στοιχείο.", Toast.LENGTH_SHORT).show();
                                updateSuggestions();
                                loadPantryItems();
                                loadSortPreference();
                            })
                            .show();
                } else {
                    // Add a new item if no similar item is found
                    dbHelper.insertPantryItem(itemName, itemQuantity);
                    Toast.makeText(this, "Προστέθηκε νέο στοιχείο.", Toast.LENGTH_SHORT).show();
                    updateSuggestions();
                    loadPantryItems();
                    loadSortPreference();
                }
            } else {
                // Groceries not updated, notify user to correct their input
                Toast.makeText(PantryActivity.this, "Ρε ΑΜΕΑ, βάλε έγκυρες τιμές", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Ακυροοο", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateSuggestions() {
        List<String> itemNames = new ArrayList<>();
        for (PantryItem item : pantryList) {
            itemNames.add(item.getName());
        }

        autoCompleteAdapter.clear();
        autoCompleteAdapter.addAll(itemNames);
        autoCompleteAdapter.notifyDataSetChanged();
    }

    private void loadSortPreference() {
        SharedPreferences preferences = getSharedPreferences("SortPreferences", MODE_PRIVATE);
        currentSortOption = preferences.getInt(SORT_PREFERENCE_KEY, 0); // Default to Auto Increment

        // Apply the saved sort method
        switch (currentSortOption) {
            case 0:
                sortListByUniqueId(false); // Auto Increment
                break;
            case 1:
                sortListByUniqueId(true); // Reverse Auto Increment
                break;
            case 2:
                sortListAlphabetically(true); // A to Z
                break;
            case 3:
                sortListAlphabetically(false); // Z to A
                break;
        }
    }

//    // Method to set the pantry updated flag in SharedPreferences
//    private void setPantryUpdatedFlag() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(PANTRY_UPDATED_KEY, true); // Set the flag to true
//        editor.apply();
//    }
//
//    public void onPantryItemRemoved() {
//        setPantryUpdatedFlag();
//    }

    // Find similar items based on Levenshtein distance
    private PantryItem findSimilarItem(List<PantryItem> existingItems, String newItemName) {
        final int SIMILARITY_THRESHOLD = 3; // Adjust this value as needed
        PantryItem closestMatch = null;
        int closestDistance = Integer.MAX_VALUE;

        for (PantryItem item : existingItems) {
            int distance = calculateLevenshteinDistance(newItemName.toLowerCase(), item.getName().toLowerCase());
            if (distance < SIMILARITY_THRESHOLD && distance < closestDistance) {
                closestMatch = item;
                closestDistance = distance;
            }
        }

        return closestMatch;
    }

    // Levenshtein distance algorithm
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

}


