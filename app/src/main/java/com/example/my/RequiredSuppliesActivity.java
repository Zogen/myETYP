package com.example.my;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_required_supplies);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Προσθήκη επιθυμητού αποθέματος");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText supplyNameInput = new EditText(this);
        supplyNameInput.setHint("Ονομα");
        layout.addView(supplyNameInput);

        final EditText supplyQuantityInput = new EditText(this);
        supplyQuantityInput.setHint("Ποσότητα");
        supplyQuantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(supplyQuantityInput);

        builder.setView(layout);

        builder.setPositiveButton("Προσθήκη", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String supplyName = supplyNameInput.getText().toString();
                String supplyQuantityStr = supplyQuantityInput.getText().toString();

                if (!supplyName.isEmpty() && !supplyQuantityStr.isEmpty()) {
                    int supplyQuantity = Integer.parseInt(supplyQuantityStr);
                    dbHelper.insertRequiredSupply(supplyName, supplyQuantity);
                    loadRequiredSupplies(); // Refresh the list
                    Toast.makeText(RequiredSuppliesActivity.this, "Επιτυχής προσθήκη", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RequiredSuppliesActivity.this, "Είσαι Ι6", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Ακυρο", (dialog, which) -> dialog.cancel());

        builder.show();
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
