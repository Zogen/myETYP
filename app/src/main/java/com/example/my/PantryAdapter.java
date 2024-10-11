package com.example.my;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final Context context;
    private List<PantryItem> pantryList;
    private DatabaseHelper dbHelper; // Interface for change listener

    public PantryAdapter(Context context, List<PantryItem> pantryList, DatabaseHelper dbHelper) {
        this.context = context;
        this.pantryList = pantryList;
        this.dbHelper = dbHelper; // Initialize listener
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryList.get(position);

        // Set the pantry item name
        holder.pantryItemName.setText(item.getName());
        holder.pantryItemQuantity.setText(String.valueOf(item.getQuantity()));

        holder.decrementButton.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                // Decrease quantity by 1
                int newQuantity = currentQuantity - 1;
                item.setQuantity(newQuantity);
                dbHelper.updatePantryItemQuantity(item.getId(), newQuantity);
                notifyItemChanged(position);
            } else {
                // If quantity is 1, remove the item
                dbHelper.deletePantryItem(item.getId());
                pantryList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, pantryList.size());
                Toast.makeText(context, "Eίχαμε " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the "+" button to increase quantity
        holder.incrementButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            dbHelper.updatePantryItem(item);
            dbHelper.updatePantryItemQuantity(item.getId(), newQuantity);
            notifyItemChanged(position);
        });


        // Set click listener for the "X" button to remove item
        holder.removePantryItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove item from database
                dbHelper.deletePantryItem(item.getId());
                // Remove item from list and notify adapter
                pantryList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, pantryList.size());
            }
        });

        // Long press listener to update item name and quantity
        holder.itemView.setOnLongClickListener(v -> {
            showEditPantryItemDialog(item, position);
            return true;
        });
    }

    private void showEditPantryItemDialog(PantryItem pantryItem, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Επεξεργασία αντικειμένου");

        // Set up the input fields
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText itemNameInput = new EditText(context);
        itemNameInput.setHint("Ονομα");
        itemNameInput.setText(pantryItem.getName());
        layout.addView(itemNameInput);

        final EditText itemQuantityInput = new EditText(context);
        itemQuantityInput.setHint("Ποσότητα");
        itemQuantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        itemQuantityInput.setText(String.valueOf(pantryItem.getQuantity()));
        layout.addView(itemQuantityInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Αποθήκευση", (dialog, which) -> {
            String newName = itemNameInput.getText().toString();
            String newQuantityStr = itemQuantityInput.getText().toString();

            if (!newName.isEmpty() && !newQuantityStr.isEmpty() && Integer.parseInt(newQuantityStr) > 0) {
                int newQuantity = Integer.parseInt(newQuantityStr);

                // Update pantry item with new values
                pantryItem.setName(newName);
                pantryItem.setQuantity(newQuantity);
                dbHelper.updatePantryItem(pantryItem);
                dbHelper.updatePantryItemQuantity(pantryItem.getId(), newQuantity);

                // Notify the adapter about the change
                pantryList.set(position, pantryItem);
                notifyItemChanged(position);
                Toast.makeText(context, "Επιτυχής ενημέρωση", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Ρε ΑΜΕΑ, βάλε έγκυρες τιμές", Toast.LENGTH_SHORT).show();
            }
        });


        builder.show();
    }

    @Override
    public int getItemCount() {
        return pantryList.size();
    }


    public static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView pantryItemName, pantryItemQuantity;
        ImageButton removePantryItemButton, decrementButton, incrementButton;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            pantryItemName = itemView.findViewById(R.id.pantryItemName);
            pantryItemQuantity = itemView.findViewById(R.id.pantryItemQuantity);
            removePantryItemButton = itemView.findViewById(R.id.removePantryItemButton);
            decrementButton = itemView.findViewById(R.id.decrementButton);
            incrementButton = itemView.findViewById(R.id.incrementButton);
        }
    }

}
