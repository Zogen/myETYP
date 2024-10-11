package com.example.my;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionHistoryActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView transactionRecyclerView;
    private TransactionAdapter adapter;
    private List<TransactionItem> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // Set up the action bar with the up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ιστορικό ΕΤΥΠ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize components
        dbHelper = new DatabaseHelper(this);
        transactionRecyclerView = findViewById(R.id.transactionRecyclerView);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(getGroupedTransactions(), this);
        transactionRecyclerView.setAdapter(adapter);

        // Load transactions
        loadTransactionHistory();
    }

    private void loadTransactionHistory() {
        Cursor cursor = dbHelper.getAllTransactions();
        transactionList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                transactionList.add(new TransactionItem(name, quantity, date));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private List<Transaction> getGroupedTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Cursor cursor = dbHelper.getAllTransactions(); // Fetch all transaction items

        List<TransactionItem> transactionItems = dbHelper.getTransactionItemsFromCursor(cursor); // Convert Cursor to List

        Map<String, Transaction> transactionMap = new HashMap<>();

        // Formatter to truncate timestamp down to minutes (ignores seconds and milliseconds)
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // Input format from DB
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());  // Output format

        for (TransactionItem item : transactionItems) {
            String originalTimestamp = item.getDate(); // Original timestamp from the database

            try {
                // Parse the original timestamp and format it to only include date and minute
                String truncatedTimestamp = outputFormat.format(inputFormat.parse(originalTimestamp));

                // Check if a transaction for this minute already exists
                if (!transactionMap.containsKey(truncatedTimestamp)) {
                    Transaction newTransaction = new Transaction(truncatedTimestamp);
                    transactionMap.put(truncatedTimestamp, newTransaction);
                    transactions.add(newTransaction);
                }

                // Add the current item to the transaction
                Transaction transaction = transactionMap.get(truncatedTimestamp);
                transaction.addItem(item);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return transactions;
    }

}
