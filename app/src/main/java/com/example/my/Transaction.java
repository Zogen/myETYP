package com.example.my;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private String timestamp;
    private List<TransactionItem> items;

    public Transaction(String timestamp) {
        this.timestamp = timestamp;
        this.items = new ArrayList<>();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<TransactionItem> getItems() {
        return items;
    }

    public void addItem(TransactionItem item) {
        items.add(item);
    }
}

