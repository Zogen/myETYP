package com.example.my;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RequiredSuppliesAdapter extends RecyclerView.Adapter<RequiredSuppliesAdapter.ViewHolder> {

    private Context context;
    private List<RequiredSupplyItem> suppliesList;
    private DatabaseHelper dbHelper;

    public RequiredSuppliesAdapter(Context context, List<RequiredSupplyItem> suppliesList, DatabaseHelper dbHelper) {
        this.context = context;
        this.suppliesList = suppliesList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_required_supply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequiredSupplyItem supplyItem = suppliesList.get(position);
        holder.supplyNameTextView.setText(supplyItem.getName());
        holder.supplyQuantityTextView.setText(String.valueOf(supplyItem.getDesiredQuantity()));

        // Long press listener to update item name and quantity
        holder.itemView.setOnLongClickListener(v -> {
            showEditSupplyItemDialog(supplyItem, position);
            return true;
        });

    }

    private void showEditSupplyItemDialog(RequiredSupplyItem item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Επεξεργασία αντικειμένου");

        // Set up the input fields
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText itemNameInput = new EditText(context);
        itemNameInput.setHint("Ονομα");
        itemNameInput.setText(item.getName());
        layout.addView(itemNameInput);

        final EditText itemQuantityInput = new EditText(context);
        itemQuantityInput.setHint("Ποσότητα");
        itemQuantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        itemQuantityInput.setText(String.valueOf(item.getDesiredQuantity()));
        layout.addView(itemQuantityInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Αποθήκευση", (dialog, which) -> {
            String newName = itemNameInput.getText().toString();
            String newQuantityStr = itemQuantityInput.getText().toString();

            if (!newName.isEmpty() && !newQuantityStr.isEmpty() && Integer.parseInt(newQuantityStr) > 0) {
                int newQuantity = Integer.parseInt(newQuantityStr);

                // Update etyp item with new values
                item.setName(newName);
                item.setQuantity(newQuantity);
                dbHelper.updateSupplyItem(item);
                dbHelper.updateSupplyItemQuantity(item.getId(), newQuantity);

                // Notify the adapter about the change
                suppliesList.set(position, item);
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
        return suppliesList.size();
    }

    public void setFilter(ArrayList<RequiredSupplyItem> newList){
        suppliesList = new ArrayList<>();
        suppliesList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView supplyNameTextView;
        TextView supplyQuantityTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            supplyNameTextView = itemView.findViewById(R.id.supplyName);
            supplyQuantityTextView = itemView.findViewById(R.id.supplyQuantity);
        }
    }
}
