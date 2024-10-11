package com.example.my;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactions;
    private Context context;

    public TransactionAdapter(List<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction.getTimestamp()); // Bind the timestamp for formatting

        holder.itemView.setOnClickListener(v -> {
            showTransactionDetails(transaction);
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView timestampTextView;

        ViewHolder(View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestamp_text); // Assuming this ID exists in your layout
        }

        public void bind(String timestamp) {
            // Input format is expected to be 'yyyy-MM-dd HH:mm:ss'
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            try {
                // Parse the incoming timestamp
                String formattedDate = outputFormat.format(inputFormat.parse(timestamp));
                // Set the formatted date with "errand" prefix
                timestampTextView.setText("Δρομολόγιο " + formattedDate);
            } catch (Exception e) {
                // Fallback to the original timestamp if parsing fails
                e.printStackTrace();
                timestampTextView.setText("Δρομολόγιο " + timestamp); // Fallback if parsing fails
            }
        }

    }

    private void showTransactionDetails(Transaction transaction) {
        // Inflate the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_transaction_details, null);

        // Get references to the TextViews in the custom layout
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView detailsTextView = dialogView.findViewById(R.id.dialog_transaction_details);

        // Build the transaction details string
        StringBuilder details = new StringBuilder();
        for (TransactionItem item : transaction.getItems()) {
            details.append(item.getName()).append(": ").append(item.getQuantity()).append("\n");
        }

        // Set the transaction details text in the TextView
        detailsTextView.setText(details.toString());

        // Show the dialog
        new AlertDialog.Builder(context)
                .setView(dialogView) // Use custom view for the dialog
                .setPositiveButton("OK", null)
                .show();
    }

}
