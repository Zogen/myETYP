package com.example.my;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EtypListActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView groceryRecyclerView;
    private EtypAdapter adapter;
    private List<EtypItem> groceryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

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

    // Dialog to add item to grocery list, similar to pantry
    private void showAddGroceryItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Προσθήκη στη λίστα ΕΤΥΠ");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText itemNameInput = new EditText(this);
        itemNameInput.setHint("Ονομα");
        layout.addView(itemNameInput);

        final EditText itemQuantityInput = new EditText(this);
        itemQuantityInput.setHint("Ποσότητα");
        itemQuantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(itemQuantityInput);

        builder.setView(layout);

        builder.setPositiveButton("Προσθήκη", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = itemNameInput.getText().toString();
                String itemQuantityStr = itemQuantityInput.getText().toString();

                // check that both fields have been filled, and that quantity is > 0
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
                    loadGroceryItems();
                } else {
                    // Groceries not updated, notify user to correct their input
                    Toast.makeText(EtypListActivity.this, "Ρε ΑΜΕΑ, βάλε έγκυρες τιμές", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Cancel button
        builder.setNegativeButton("Ακυροοο", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // This method is to be used when the user has bought groceries,
    // and will move all items from the grocery list to the pantry
    private void moveGroceryItemsToPantry() {
        if (!groceryList.isEmpty()) {
            for (EtypItem etypItem : groceryList) {
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

            // Clear the grocery list and refresh the view
            groceryList.clear();
            adapter.notifyDataSetChanged();

            // Notify user
            Toast.makeText(this, "Προστέθηκαν στο ντουλάπι", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Αφού είναι άδεια η λίστα ρε γιωτά", Toast.LENGTH_SHORT).show();
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
