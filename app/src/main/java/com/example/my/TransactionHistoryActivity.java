package com.example.my;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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
        adapter = new TransactionAdapter(this, transactionList);
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
}
