package com.example.my;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EtypListActivity extends BaseActivity implements SearchView.OnQueryTextListener{

    private DatabaseHelper dbHelper;
    private RecyclerView groceryRecyclerView;
    private EtypAdapter adapter;
    private List<EtypItem> groceryList;
    private static final String SORT_PREFERENCE_KEY = "SortPreference_Grocery";
    private ArrayAdapter<String> autoCompleteAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());

        // display action bar, with up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Λίστα ΕΤΥΠ"); // Set the title to the name of the activity
            actionBar.setDisplayHomeAsUpEnabled(true); // Enable the Up button
        }

        // initializing activity components
        dbHelper = new DatabaseHelper(this);
        groceryRecyclerView = findViewById(R.id.groceryRecyclerView);
        groceryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groceryList = new ArrayList<>();
        adapter = new EtypAdapter(this, groceryList, dbHelper);
        groceryRecyclerView.setAdapter(adapter);

        // method to load items registered in Pantry db table, implemented below
        loadGroceryItems();
        loadSortPreference();

        // button to add item to grocery list
        Button addGroceryItemButton = findViewById(R.id.addGroceryItemButton);
        addGroceryItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddGroceryItemDialog();
            }
        });

        // this button moves the contents of the grocery list to the pantry, simulating groceries purchase, implementation below
        Button moveToPantryButton = findViewById(R.id.moveToPantryButton);
        moveToPantryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveGroceryItemsToPantry();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_etyp_list, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Make the SearchView expand to the full width
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Set up listener to handle search text changes
        searchView.setOnQueryTextListener(this);

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
            Collections.sort(groceryList, (item1, item2) -> Integer.compare(item2.getId(), item1.getId()));
        } else {
            Collections.sort(groceryList, (item1, item2) -> Integer.compare(item1.getId(), item2.getId()));
        }
        adapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
    }

    private void sortListAlphabetically(boolean ascending) {
        if (ascending) {
            Collections.sort(groceryList, (item1, item2) -> item1.getName().compareToIgnoreCase(item2.getName()));
        } else {
            Collections.sort(groceryList, (item1, item2) -> item2.getName().compareToIgnoreCase(item1.getName()));
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
        ArrayList<EtypItem> newList = new ArrayList<>();
        for (EtypItem item : groceryList)
        {
            String name = item.getName().toLowerCase();
            if (name.contains(newText)){
                newList.add(item);
            }
        }
        adapter.setFilter(newList);
        return  true;
    }

    private void loadGroceryItems() {
        Cursor cursor = dbHelper.getAllGroceryItems();
        groceryList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                groceryList.add(new EtypItem(id, name, quantity));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddGroceryItemDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null); // Use your XML layout

        // Get references to the input fields
        AutoCompleteTextView itemNameInput = dialogView.findViewById(R.id.item_name_input);
        EditText itemQuantityInput = dialogView.findViewById(R.id.item_quantity_input);

        // Prepare suggestions based on the current grocery list
        List<String> itemNames = new ArrayList<>();
        for (EtypItem item : groceryList) {
            itemNames.add(item.getName());
        }

        // Set up the AutoCompleteTextView with suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, itemNames);
        itemNameInput.setAdapter(adapter);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Προσθήκη στη λίστα ΕΤΥΠ");
        builder.setView(dialogView);

        builder.setPositiveButton("Προσθήκη", (dialog, which) -> {
            String itemName = itemNameInput.getText().toString().trim();
            String itemQuantityStr = itemQuantityInput.getText().toString().trim();

            // Check that both fields have been filled and that quantity is > 0
            if (!itemName.isEmpty() && !itemQuantityStr.isEmpty()) {
                int itemQuantity = Integer.parseInt(itemQuantityStr);
                EtypItem existingEtypItem = dbHelper.getGroceryItemByName(itemName);
                if (existingEtypItem != null) {
                    // Item exists, update its quantity
                    int newQuantity = existingEtypItem.getQuantity() + itemQuantity;
                    existingEtypItem.setQuantity(newQuantity);
                    dbHelper.updateGroceryItemQuantity(existingEtypItem.getId(), newQuantity);
                } else {
                    // Item does not exist, insert it as a new item
                    dbHelper.insertGroceryItem(itemName, itemQuantity);
                }
                // Groceries updated successfully
                Toast.makeText(EtypListActivity.this, "Επιτυχής προσθήκη/ενημέρωση", Toast.LENGTH_SHORT).show();
                updateSuggestions();
                loadGroceryItems();
            } else {
                // Groceries not updated, notify user to correct their input
                Toast.makeText(EtypListActivity.this, "Ρε ΑΜΕΑ, βάλε έγκυρες τιμές", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Ακυροοο", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateSuggestions() {
        List<String> itemNames = new ArrayList<>();
        for (EtypItem item : groceryList) {
            itemNames.add(item.getName());
        }

        autoCompleteAdapter.clear();
        autoCompleteAdapter.addAll(itemNames);
        autoCompleteAdapter.notifyDataSetChanged();
    }


    // This method is to be used when the user has bought groceries,
    // and will move all items from the grocery list to the pantry
    private void moveGroceryItemsToPantry() {
        // Create a list to hold the items to be moved
        List<EtypItem> itemsToMove = new ArrayList<>();

        // Check if any items are highlighted
        for (EtypItem etypItem : groceryList) {
            if (etypItem.isChecked()) {
                itemsToMove.add(etypItem); // Add checked items to the list
            }
        }

        // If no items are highlighted, move all items
        if (itemsToMove.isEmpty()) {
            itemsToMove.addAll(groceryList); // Add all items if none are selected
        }

        // Move the selected or all items
        if (!itemsToMove.isEmpty()) {
            for (EtypItem etypItem : itemsToMove) {
                String itemName = etypItem.getName();
                int groceryQuantity = etypItem.getQuantity();

                // Check if the item already exists in the pantry
                PantryItem pantryItem = dbHelper.getPantryItemByName(itemName);

                if (pantryItem != null) {
                    // Item exists in the pantry, update its quantity
                    int newQuantity = pantryItem.getQuantity() + groceryQuantity;
                    dbHelper.updatePantryItemQuantity(pantryItem.getId(), newQuantity);
                } else {
                    // Item does not exist, add it to the pantry
                    dbHelper.insertPantryItem(itemName, groceryQuantity);
                }

                // Log the transaction in the history
                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                dbHelper.insertTransactionHistory(itemName, groceryQuantity, currentDate);

                // Remove the item from the grocery list
                dbHelper.deleteGroceryItem(etypItem.getId());
            }

            // Clear the selected items from the grocery list
            groceryList.removeAll(itemsToMove);
            adapter.notifyDataSetChanged();

            // Notify user
            Toast.makeText(this, "Προστέθηκαν στο ντουλάπι", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Αφού είναι άδεια η λίστα ρε γιωτά", Toast.LENGTH_SHORT).show();
        }
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

//    // Override to add menu
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return true;
//    }
//
//    // Handle menu item clicks
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_transaction_history) {
//            // Launch the TransactionHistoryActivity to view transaction history
//            Intent intent = new Intent(this, TransactionHistoryActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (id == R.id.action_app_info) {
//            // Show app information (TBD)
//            showAppInfo();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    // Placeholder method for showing app info
//    private void showAppInfo() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("App Info")
//                .setMessage("Version: 1.0\nHow to Use: TBD")
//                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
//                .show();
//    }

}
