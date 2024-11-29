package com.example.my;

import android.app.AlertDialog;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EtypAdapter extends RecyclerView.Adapter<EtypAdapter.EtypViewHolder> {

    private Context context;
    private List<EtypItem> etypList;  // List of items to display
    private DatabaseHelper dbHelper;  // Helper class for database operations

    // Constructor to initialize context, item list and database helper
    public EtypAdapter(Context context, List<EtypItem> etypList, DatabaseHelper dbHelper) {
        this.context = context;
        this.etypList = etypList;
        this.dbHelper = dbHelper;
    }

    // Create new view holder when a new item is needed
    @NonNull
    @Override
    public EtypViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each list item
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new EtypViewHolder(view);
    }

    // Bind data to the view holder at a specific position in the list
    @Override
    public void onBindViewHolder(@NonNull EtypViewHolder holder, int position) {
        EtypItem item = etypList.get(position);  // Get the item at the current position

        // Set the item name and quantity in the corresponding views
        holder.itemName.setText(item.getName());
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));

        // Change the card background color and elevation if the item is checked
        if (item.isChecked()) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_highlight));  // Highlight color
            holder.cardView.setCardElevation(8f);  // Elevate the card when checked
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg));  // Default color
            holder.cardView.setCardElevation(4f);  // Default elevation
        }

        // Set click listener to toggle the item's checked state
        holder.itemView.setOnClickListener(v -> {
            item.setChecked(!item.isChecked());  // Toggle the checked state
            notifyItemChanged(position);  // Notify the adapter that the item has changed
        });

        // Decrement quantity button listener
        holder.decrementButton.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                // Decrease quantity by 1
                int newQuantity = currentQuantity - 1;
                item.setQuantity(newQuantity);
                dbHelper.updateEtypItem(item);  // Update item in the database
                dbHelper.updateEtypItemQuantity(item.getId(), newQuantity);  // Update quantity in the database
                notifyItemChanged(position);  // Notify the adapter to update the UI
            } else {
                // If quantity is 1, remove the item from the list
                dbHelper.deleteEtypItem(item.getId());  // Delete item from the database
                etypList.remove(position);  // Remove item from the list
                notifyItemRemoved(position);  // Notify the adapter to remove the item
                notifyItemRangeChanged(position, etypList.size());  // Update the item positions
                Toast.makeText(context, "Δεν χρειαζόμαστε " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Increment quantity button listener
        holder.incrementButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;  // Increase quantity by 1
            item.setQuantity(newQuantity);
            dbHelper.updateEtypItem(item);  // Update item in the database
            dbHelper.updateEtypItemQuantity(item.getId(), newQuantity);  // Update quantity in the database
            notifyItemChanged(position);  // Notify the adapter to update the UI
        });

        // Remove item button listener
        holder.removeItemButton.setOnClickListener(v -> {
            dbHelper.deleteEtypItem(item.getId());  // Delete item from the database
            etypList.remove(position);  // Remove item from the list
            notifyItemRemoved(position);  // Notify the adapter to remove the item
            notifyItemRangeChanged(position, etypList.size());  // Update the item positions
        });

        // Long press listener to edit item details
        holder.itemView.setOnLongClickListener(v -> {
            showEditEtypItemDialog(item, position);  // Show the dialog to edit item
            return true;  // Indicate that the event has been handled
        });
    }

    // Method to show the edit dialog to modify item details
    private void showEditEtypItemDialog(EtypItem etypItem, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Επεξεργασία αντικειμένου");

        // Create a linear layout to hold the input fields
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Input field for item name
        final EditText itemNameInput = new EditText(context);
        itemNameInput.setHint("Ονομα");
        itemNameInput.setText(etypItem.getName());  // Set current name as default
        layout.addView(itemNameInput);

        // Input field for item quantity
        final EditText itemQuantityInput = new EditText(context);
        itemQuantityInput.setHint("Ποσότητα");
        itemQuantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);  // Allow only numbers
        itemQuantityInput.setText(String.valueOf(etypItem.getQuantity()));  // Set current quantity as default
        layout.addView(itemQuantityInput);

        // Set the custom layout view
        builder.setView(layout);

        // Set the action for the "Save" button
        builder.setPositiveButton("Αποθήκευση", (dialog, which) -> {
            String newName = itemNameInput.getText().toString();
            String newQuantityStr = itemQuantityInput.getText().toString();

            // Check if the input is valid
            if (!newName.isEmpty() && !newQuantityStr.isEmpty() && Integer.parseInt(newQuantityStr) > 0) {
                int newQuantity = Integer.parseInt(newQuantityStr);

                // Update item with the new values
                etypItem.setName(newName);
                etypItem.setQuantity(newQuantity);
                dbHelper.updateEtypItem(etypItem);  // Update the item in the database
                dbHelper.updateEtypItemQuantity(etypItem.getId(), newQuantity);  // Update quantity in the database

                // Notify the adapter about the change
                etypList.set(position, etypItem);
                notifyItemChanged(position);  // Notify the adapter to refresh the UI
                Toast.makeText(context, "Επιτυχής ενημέρωση", Toast.LENGTH_SHORT).show();
            } else {
                // Show error if inputs are invalid
                Toast.makeText(context, "Ρε ΑΜΕΑ, βάλε έγκυρες τιμές", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();  // Show the dialog
    }

    // Return the number of items in the list
    @Override
    public int getItemCount() {
        return etypList.size();
    }

    // Method to update the list with a filtered list
    public void setFilter(ArrayList<EtypItem> newList) {
        etypList = new ArrayList<>();
        etypList.addAll(newList);
        notifyDataSetChanged();  // Notify the adapter to refresh the list
    }

    // ViewHolder to hold references to views for each item
    public static class EtypViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity;
        ImageButton removeItemButton, decrementButton, incrementButton;
        CardView cardView;

        // Constructor to initialize the view holder with item views
        public EtypViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            removeItemButton = itemView.findViewById(R.id.removeItemButton);
            decrementButton = itemView.findViewById(R.id.decrementButton);
            incrementButton = itemView.findViewById(R.id.incrementButton);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}
