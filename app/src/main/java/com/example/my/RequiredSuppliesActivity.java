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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RequiredSuppliesActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView suppliesRecyclerView;
    private RequiredSuppliesAdapter adapter;
    private List<RequiredSupplyItem> suppliesList;

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
}
