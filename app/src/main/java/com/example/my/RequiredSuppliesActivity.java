package com.example.my;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequiredSuppliesActivity extends BaseActivity implements SearchView.OnQueryTextListener{

    private DatabaseHelper dbHelper;
    private RecyclerView suppliesRecyclerView;
    private RequiredSuppliesAdapter adapter;
    private List<RequiredSupplyItem> suppliesList;
    private static final String SORT_PREFERENCE_KEY = "SortPreference_Supplies";
    private ArrayAdapter<String> autoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_required_supplies);

        autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());

        // Display action bar with title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Απαιτούμενο Απόθεμα");
            actionBar.setDisplayHomeAsUpEnabled(true); // Enable the Up button
        }

        // Initialize activity components
        dbHelper = new DatabaseHelper(this);
        suppliesRecyclerView = findViewById(R.id.suppliesRecyclerView);
        suppliesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suppliesList = new ArrayList<>();
        adapter = new RequiredSuppliesAdapter(this, suppliesList, dbHelper);
        suppliesRecyclerView.setAdapter(adapter);

        // Button to add required supply
        Button addSupplyButton = findViewById(R.id.addSupplyButton);
        addSupplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSupplyDialog();
            }
        });

        // Button to check inventory
        Button checkInventoryButton = findViewById(R.id.checkInventoryButton);
        checkInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInventoryAndUpdateEtypList();
            }
        });

        // Load existing supplies from the database
        loadRequiredSupplies();
        loadSortPreference();
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
            loadRequiredSupplies();  // Reload entire list when search is closed
            return false;
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
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
            Collections.sort(suppliesList, (item1, item2) -> Integer.compare(item2.getId(), item1.getId()));
        } else {
            Collections.sort(suppliesList, (item1, item2) -> Integer.compare(item1.getId(), item2.getId()));
        }
        adapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
    }

    private void sortListAlphabetically(boolean ascending) {
        if (ascending) {
            Collections.sort(suppliesList, (item1, item2) -> item1.getName().compareToIgnoreCase(item2.getName()));
        } else {
            Collections.sort(suppliesList, (item1, item2) -> item2.getName().compareToIgnoreCase(item1.getName()));
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
        ArrayList<RequiredSupplyItem> newList = new ArrayList<>();
        for (RequiredSupplyItem item : suppliesList)
        {
            String name = item.getName().toLowerCase();
            if (name.contains(newText)){
                newList.add(item);
            }
        }
        adapter.setFilter(newList);
        return  true;
    }

    private void loadRequiredSupplies() {
        Cursor cursor = dbHelper.getAllRequiredSupplies();
        suppliesList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                suppliesList.add(new RequiredSupplyItem(id, name, quantity));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddSupplyDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null); // Use your XML layout

        // Get references to the input fields
        AutoCompleteTextView itemNameInput = dialogView.findViewById(R.id.item_name_input);
        EditText itemQuantityInput = dialogView.findViewById(R.id.item_quantity_input);

        // Prepare suggestions based on the current grocery list
        List<String> itemNames = new ArrayList<>();
        for (RequiredSupplyItem item : suppliesList) {
            itemNames.add(item.getName());
        }

        // Set up the AutoCompleteTextView with suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, itemNames);
        itemNameInput.setAdapter(adapter);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Προσθήκη αποθέματος ασφαλείας");
        builder.setView(dialogView);

        builder.setPositiveButton("Προσθήκη", (dialog, which) -> {
            String itemName = itemNameInput.getText().toString().trim();
            String itemQuantityStr = itemQuantityInput.getText().toString().trim();

            // Check that both fields have been filled and that quantity is > 0
            if (!itemName.isEmpty() && !itemQuantityStr.isEmpty()) {
                int itemQuantity = Integer.parseInt(itemQuantityStr);
                RequiredSupplyItem existingSupplyItem = dbHelper.getSupplyItemByName(itemName);
                if (existingSupplyItem != null) {
                    // Item exists, update its quantity
                    int newQuantity = existingSupplyItem.getDesiredQuantity() + itemQuantity;
                    existingSupplyItem.setQuantity(newQuantity);
                    dbHelper.updateSupplyItemQuantity(existingSupplyItem.getId(), newQuantity);
                } else {
                    // Item does not exist, insert it as a new item
                    dbHelper.insertRequiredSupply(itemName, itemQuantity);
                }
                // Groceries updated successfully
                Toast.makeText(RequiredSuppliesActivity.this, "Επιτυχής προσθήκη/ενημέρωση", Toast.LENGTH_SHORT).show();
                updateSuggestions();
                loadRequiredSupplies();
            } else {
                // Groceries not updated, notify user to correct their input
                Toast.makeText(RequiredSuppliesActivity.this, "Ρε ΑΜΕΑ, βάλε έγκυρες τιμές", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Ακυρο", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateSuggestions() {
        List<String> itemNames = new ArrayList<>();
        for (RequiredSupplyItem item : suppliesList) {
            itemNames.add(item.getName());
        }

        autoCompleteAdapter.clear();
        autoCompleteAdapter.addAll(itemNames);
        autoCompleteAdapter.notifyDataSetChanged();
    }

    private void checkInventoryAndUpdateEtypList() {
        for (RequiredSupplyItem supplyItem : suppliesList) {
            int desiredQuantity = supplyItem.getDesiredQuantity();
            int currentQuantity;
            if (dbHelper.getPantryItemByName(supplyItem.getName()) != null) {
                currentQuantity = dbHelper.getPantryItemByName(supplyItem.getName()).getQuantity();
            } else { currentQuantity = 0; }

            int quantityToAdd = desiredQuantity - currentQuantity;

            if (quantityToAdd > 0) {
                // Check if the item already exists in etyp list
                EtypItem etypItem = dbHelper.getGroceryItemByName(supplyItem.getName());

                if (etypItem != null && etypItem.getQuantity() >= quantityToAdd) {
                    continue;
                } else if (etypItem != null && etypItem.getQuantity() < quantityToAdd) {
                    dbHelper.updateGroceryItemQuantity(etypItem.getId(), quantityToAdd);
                } else if (etypItem == null) {dbHelper.insertGroceryItem(supplyItem.getName(), quantityToAdd);}

            }
        }

        Toast.makeText(this, "Οι ελλείψεις προστέθηκαν στη λίστα ΕΤΥΠ", Toast.LENGTH_SHORT).show();
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

}
