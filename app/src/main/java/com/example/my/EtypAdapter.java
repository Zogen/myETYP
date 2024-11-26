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
    private List<EtypItem> etypList;
    private DatabaseHelper dbHelper;

    public EtypAdapter(Context context, List<EtypItem> etypList, DatabaseHelper dbHelper) {
        this.context = context;
        this.etypList = etypList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public EtypViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new EtypViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtypViewHolder holder, int position) {
        EtypItem item = etypList.get(position);

        // Set item name and quantity
        holder.itemName.setText(item.getName());
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));

        // Highlight if checked, modify CardView properties
        if (item.isChecked()) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_highlight)); // Highlighted color
            holder.cardView.setCardElevation(8f); // Elevate the selected card
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg)); // Default color
            holder.cardView.setCardElevation(4f); // Default elevation
        }

        // Long click to select/deselect item
        holder.itemView.setOnClickListener(v -> {
            item.setChecked(!item.isChecked()); // Toggle checked state
            notifyItemChanged(position); // Update the item UI
        });

        holder.decrementButton.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                // Decrease quantity by 1
                int newQuantity = currentQuantity - 1;
                item.setQuantity(newQuantity);
                dbHelper.updateEtypItem(item);
                dbHelper.updateEtypItemQuantity(item.getId(), newQuantity);
                notifyItemChanged(position);
            } else {
                // If quantity is 1, remove the item
                dbHelper.deleteEtypItem(item.getId());
                etypList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, etypList.size());
                Toast.makeText(context, "Δεν χρειαζόμαστε " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the "+" button to increase quantity
        holder.incrementButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            dbHelper.updateEtypItem(item);
            dbHelper.updateEtypItemQuantity(item.getId(), newQuantity);
            notifyItemChanged(position);
        });

        // Set click listener for the "X" button to remove item
        holder.removeItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove item from database
                dbHelper.deleteEtypItem(item.getId());
                // Remove item from list and notify adapter
                etypList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, etypList.size());
            }
        });

        // Long press listener to update item name and quantity
        holder.itemView.setOnLongClickListener(v -> {
            showEditEtypItemDialog(item, position);
            return true;
        });

    }

    private void showEditEtypItemDialog(EtypItem etypItem, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Επεξεργασία αντικειμένου");

        // Set up the input fields
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText itemNameInput = new EditText(context);
        itemNameInput.setHint("Ονομα");
        itemNameInput.setText(etypItem.getName());
        layout.addView(itemNameInput);

        final EditText itemQuantityInput = new EditText(context);
        itemQuantityInput.setHint("Ποσότητα");
        itemQuantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        itemQuantityInput.setText(String.valueOf(etypItem.getQuantity()));
        layout.addView(itemQuantityInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Αποθήκευση", (dialog, which) -> {
            String newName = itemNameInput.getText().toString();
            String newQuantityStr = itemQuantityInput.getText().toString();

            if (!newName.isEmpty() && !newQuantityStr.isEmpty() && Integer.parseInt(newQuantityStr) > 0) {
                int newQuantity = Integer.parseInt(newQuantityStr);

                // Update etyp item with new values
                etypItem.setName(newName);
                etypItem.setQuantity(newQuantity);
                dbHelper.updateEtypItem(etypItem);
                dbHelper.updateEtypItemQuantity(etypItem.getId(), newQuantity);

                // Notify the adapter about the change
                etypList.set(position, etypItem);
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
        return etypList.size();
    }

    public void setFilter(ArrayList<EtypItem> newList){
        etypList = new ArrayList<>();
        etypList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class EtypViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity;
        ImageButton removeItemButton, decrementButton, incrementButton;
        CardView cardView;

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
