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
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PantryActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView pantryRecyclerView;
    private PantryAdapter adapter;
    private List<PantryItem> pantryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query); // Trigger search when "submit" is pressed
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText); // Filter list every time text changes
                return true;
            }
        });

        // Reset to full list when SearchView is closed
        searchView.setOnCloseListener(() -> {
            resetToFullList();
            return false;
        });

        return true;
    }


    private void performSearch(String query) {
        List<PantryItem> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            // If query is empty, reload the full list
            filteredList.addAll(pantryList);
        } else {
            // Otherwise, filter based on the query
            for (PantryItem item : pantryList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }

        // Update the adapter with the filtered list
        adapter.updateList(filteredList);
    }

    private void resetToFullList() {
        adapter.updateList(pantryList); // Load the full list back into the adapter
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

    //dialog for adding new item to pantry set. initiated by button press
    private void showAddPantryItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Προσθήκη αντικειμένου στο ντουλάπι");

        // Set up the input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        //item label
        final EditText itemNameInput = new EditText(this);
        itemNameInput.setHint("Ονομα");
        layout.addView(itemNameInput);

        //item quantity
        final EditText itemQuantityInput = new EditText(this);
        itemQuantityInput.setHint("Ποσότητα");
        itemQuantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(itemQuantityInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Προσθήκη", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = itemNameInput.getText().toString();
                String itemQuantityStr = itemQuantityInput.getText().toString();

                // check that both fields have been filled, and that quantity is > 0
                if (!itemName.isEmpty() && !itemQuantityStr.isEmpty() && Integer.parseInt(itemQuantityStr) > 0) {
                    int itemQuantity = Integer.parseInt(itemQuantityStr);
                    PantryItem existingPantryItem = dbHelper.getPantryItemByName(itemName);
                    if (existingPantryItem != null) {
                        // Item exists, update its quantity
                        int newQuantity = existingPantryItem.getQuantity() + itemQuantity;
                        existingPantryItem.setQuantity(newQuantity);
                        dbHelper.updatePantryItemQuantity(existingPantryItem.getId(), newQuantity);
                    } else {
                        // Item does not exist, insert it as a new item
                        dbHelper.insertPantryItem(itemName, itemQuantity);

                        // Mark that the pantry has been updated, this results in new API call in recipes activity
//                        setPantryUpdatedFlag();
                    }

                    // Update successful, notify user
                    Toast.makeText(PantryActivity.this, "Επιτυχής προσθήκη/ενημέρωση", Toast.LENGTH_SHORT).show();
                    loadPantryItems();
                } else {
                    // Error, notify user to correct their input
                    Toast.makeText(PantryActivity.this, "Ρε ΑΜΕΑ, βάλε έγκυρες τιμές", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // cancel button
        builder.setNegativeButton("Λόχος Ακυρο", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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

}
