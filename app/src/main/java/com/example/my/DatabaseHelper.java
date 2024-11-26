package com.example.my;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AppDatabase.db";
    private static final int DATABASE_VERSION = 4; // Incremented version

    // Table and column names for Pantry
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_PANTRY_ID = "id";
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QUANTITY = "quantity";

    // Table and column names for Grocery
    public static final String TABLE_GROCERY = "grocery";
    public static final String COLUMN_GROCERY_ID = "id";
    public static final String COLUMN_GROCERY_NAME = "name";
    public static final String COLUMN_GROCERY_QUANTITY = "quantity";

    // Table and column names for Transaction History
    public static final String TABLE_TRANSACTION_HISTORY = "transaction_history";
    public static final String COLUMN_TRANSACTION_ID = "id";
    public static final String COLUMN_TRANSACTION_ITEM_NAME = "name";
    public static final String COLUMN_TRANSACTION_QUANTITY = "quantity";
    public static final String COLUMN_TRANSACTION_DATE = "date";

    // Table and column names for Required Supplies
    public static final String TABLE_REQUIRED_SUPPLIES = "required_supplies";
    public static final String COLUMN_REQUIRED_SUPPLIES_ID = "id";
    public static final String COLUMN_REQUIRED_SUPPLIES_NAME = "name";
    public static final String COLUMN_REQUIRED_SUPPLIES_QUANTITY = "quantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Pantry table
        String CREATE_PANTRY_TABLE = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT, " +
                COLUMN_PANTRY_QUANTITY + " INTEGER);";
        db.execSQL(CREATE_PANTRY_TABLE);

        // Create Grocery table
        String CREATE_GROCERY_TABLE = "CREATE TABLE " + TABLE_GROCERY + " (" +
                COLUMN_GROCERY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GROCERY_NAME + " TEXT, " +
                COLUMN_GROCERY_QUANTITY + " INTEGER);";
        db.execSQL(CREATE_GROCERY_TABLE);

        // Create Transaction History table
        String CREATE_TRANSACTION_HISTORY_TABLE = "CREATE TABLE " + TABLE_TRANSACTION_HISTORY + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANSACTION_ITEM_NAME + " TEXT, " +
                COLUMN_TRANSACTION_QUANTITY + " INTEGER, " +
                COLUMN_TRANSACTION_DATE + " TEXT);";
        db.execSQL(CREATE_TRANSACTION_HISTORY_TABLE);

        // Create Required Supplies table
        String CREATE_REQUIRED_SUPPLIES_TABLE = "CREATE TABLE " + TABLE_REQUIRED_SUPPLIES + " (" +
                COLUMN_REQUIRED_SUPPLIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_REQUIRED_SUPPLIES_NAME + " TEXT, " +
                COLUMN_REQUIRED_SUPPLIES_QUANTITY + " INTEGER);";
        db.execSQL(CREATE_REQUIRED_SUPPLIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            String CREATE_REQUIRED_SUPPLIES_TABLE = "CREATE TABLE " + TABLE_REQUIRED_SUPPLIES + " (" +
                    COLUMN_REQUIRED_SUPPLIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_REQUIRED_SUPPLIES_NAME + " TEXT, " +
                    COLUMN_REQUIRED_SUPPLIES_QUANTITY + " INTEGER);";
            db.execSQL(CREATE_REQUIRED_SUPPLIES_TABLE);
        }
    }

    // Insert a new item into the pantry
    public void insertPantryItem(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_NAME, name);
        values.put(COLUMN_PANTRY_QUANTITY, quantity);
        db.insert(TABLE_PANTRY, null, values);
        db.close();
    }

    // Insert a new item into the grocery list
    public void insertEtypItem(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROCERY_NAME, name);
        values.put(COLUMN_GROCERY_QUANTITY, quantity);
        db.insert(TABLE_GROCERY, null, values);
        db.close();
    }

    // Get all pantry items
    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PANTRY_ID + " AS _id, " + COLUMN_PANTRY_NAME + ", " + COLUMN_PANTRY_QUANTITY + " FROM " + TABLE_PANTRY;
        return db.rawQuery(query, null);
    }

    // Get all grocery items
    public Cursor getAllEtypItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_GROCERY_ID + " AS _id, " + COLUMN_GROCERY_NAME + ", " + COLUMN_GROCERY_QUANTITY + " FROM " + TABLE_GROCERY;
        return db.rawQuery(query, null);
    }

    // Delete a pantry item
    public void deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, COLUMN_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete a grocery item
    public void deleteEtypItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROCERY, COLUMN_GROCERY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete a grocery item
    public void deleteRequiredItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REQUIRED_SUPPLIES, COLUMN_REQUIRED_SUPPLIES_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Look for an existing item in the Pantry Table
    public PantryItem getPantryItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PANTRY,
                null,
                COLUMN_PANTRY_NAME + " = ?",
                new String[]{name},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_ID));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_QUANTITY));
            cursor.close();
            return new PantryItem(id, name, quantity);
        }

        if (cursor != null) {
            cursor.close();
        }
        return null; // Item not found
    }

    // Update quantity of an existing pantry item
    public void updatePantryItemQuantity(int id, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_QUANTITY, newQuantity);
        db.update(TABLE_PANTRY, values, COLUMN_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Update quantity
    public void updatePantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_NAME, item.getName());

        db.update(TABLE_PANTRY, values, COLUMN_PANTRY_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public void updateEtypItem(EtypItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROCERY_NAME, item.getName());

        db.update(TABLE_GROCERY, values, COLUMN_GROCERY_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public void updateSupplyItem(RequiredSupplyItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REQUIRED_SUPPLIES_NAME, item.getName());

        db.update(TABLE_REQUIRED_SUPPLIES, values, COLUMN_GROCERY_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public void updateSupplyItemQuantity(int id, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REQUIRED_SUPPLIES_QUANTITY, newQuantity);
        db.update(TABLE_REQUIRED_SUPPLIES, values, COLUMN_GROCERY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Retrieve grocery item by name
    public EtypItem getEtypItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_GROCERY,
                null,
                COLUMN_GROCERY_NAME + " = ?",
                new String[]{name},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GROCERY_ID));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GROCERY_QUANTITY));
            cursor.close();
            return new EtypItem(id, name, quantity);
        }

        if (cursor != null) {
            cursor.close();
        }
        return null; // Item not found
    }

    // Update quantity of existing grocery item
    public void updateEtypItemQuantity(int id, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROCERY_QUANTITY, newQuantity);
        db.update(TABLE_GROCERY, values, COLUMN_GROCERY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Get a list of items in Pantry table
    public List<String> getPantryItems() {
        List<String> pantryItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PANTRY_NAME + " FROM " + TABLE_PANTRY, null);

        if (cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_NAME));
                pantryItems.add(itemName);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pantryItems;
    }

    // Insert a new transaction into the history table
    public void insertTransactionHistory(String name, int quantity, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_ITEM_NAME, name);
        values.put(COLUMN_TRANSACTION_QUANTITY, quantity);
        values.put(COLUMN_TRANSACTION_DATE, date);
        db.insert(TABLE_TRANSACTION_HISTORY, null, values);
        db.close();
    }

    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT name, quantity, date FROM transaction_history", null);
    }

    public List<TransactionItem> getTransactionItemsFromCursor(Cursor cursor) {
        List<TransactionItem> transactionItems = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndex("name")); // Check column name
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity")); // Check column name
                String date = cursor.getString(cursor.getColumnIndex("date")); // Check column name
                transactionItems.add(new TransactionItem(itemName, quantity, date));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close(); // Always close the cursor to avoid memory leaks
        }

        return transactionItems;
    }



    // Insert a required supply item
    public void insertRequiredSupply(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REQUIRED_SUPPLIES_NAME, name);
        values.put(COLUMN_REQUIRED_SUPPLIES_QUANTITY, quantity);
        db.insert(TABLE_REQUIRED_SUPPLIES, null, values);
        db.close();
    }

    // Get all required supplies
    public Cursor getAllRequiredSupplies() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_REQUIRED_SUPPLIES_ID + " AS _id, " +
                COLUMN_REQUIRED_SUPPLIES_NAME + ", " +
                COLUMN_REQUIRED_SUPPLIES_QUANTITY + " FROM " + TABLE_REQUIRED_SUPPLIES;
        return db.rawQuery(query, null);
    }

    // Retrieve grocery item by name
    public RequiredSupplyItem getSupplyItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_REQUIRED_SUPPLIES,
                null,
                COLUMN_REQUIRED_SUPPLIES_NAME + " = ?",
                new String[]{name},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQUIRED_SUPPLIES_ID));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQUIRED_SUPPLIES_QUANTITY));
            cursor.close();
            return new RequiredSupplyItem(id, name, quantity);
        }

        if (cursor != null) {
            cursor.close();
        }
        return null; // Item not found
    }

    // Get quantity of a specific supply
    public int getRequiredSupplyQuantity(String supplyName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REQUIRED_SUPPLIES, new String[]{COLUMN_REQUIRED_SUPPLIES_QUANTITY}, COLUMN_REQUIRED_SUPPLIES_NAME + "=?", new String[]{supplyName}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_REQUIRED_SUPPLIES_QUANTITY));
            cursor.close();
            return quantity;
        }
        return 0; // Default if not found
    }

    public void addToEtypList(String name, int quantityToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                TABLE_GROCERY,
                new String[]{COLUMN_GROCERY_QUANTITY},
                COLUMN_GROCERY_NAME + "=?",
                new String[]{name},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // Item exists, update the quantity
            int currentQuantity = cursor.getInt(cursor.getColumnIndex(COLUMN_GROCERY_QUANTITY));
            int newQuantity = currentQuantity + quantityToAdd;

            ContentValues values = new ContentValues();
            values.put(COLUMN_GROCERY_QUANTITY, newQuantity);
            db.update(TABLE_GROCERY, values, COLUMN_GROCERY_NAME + "=?", new String[]{name});
        } else {
            // Item does not exist, insert new item
            ContentValues values = new ContentValues();
            values.put(COLUMN_GROCERY_NAME, name);
            values.put(COLUMN_GROCERY_QUANTITY, quantityToAdd);
            db.insert(TABLE_GROCERY, null, values);
        }

        // Close the cursor and database connection
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    public List<RequiredSupply> getFilteredRequiredSupplies() {
        List<RequiredSupply> filteredSupplies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get all required supplies
        Cursor requiredCursor = db.rawQuery("SELECT * FROM " + TABLE_REQUIRED_SUPPLIES, null);

        if (requiredCursor.moveToFirst()) {
            do {
                String name = requiredCursor.getString(requiredCursor.getColumnIndexOrThrow(COLUMN_REQUIRED_SUPPLIES_NAME));
                int requiredQuantity = requiredCursor.getInt(requiredCursor.getColumnIndexOrThrow(COLUMN_REQUIRED_SUPPLIES_QUANTITY));

                // Check if there is an equal or greater quantity in the pantry
                int pantryQuantity = getPantryItemByName(name).getQuantity();

                // Only add to filtered supplies if pantry does not meet or exceed required quantity
                if (pantryQuantity < requiredQuantity) {
                    filteredSupplies.add(new RequiredSupply(name, requiredQuantity));
                }
            } while (requiredCursor.moveToNext());
        }

        requiredCursor.close();
        return filteredSupplies;
    }

    // RequiredSupply class to hold the required supplies data
    public static class RequiredSupply {
        private String name;
        private int quantity;

        public RequiredSupply(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    // Method to export the database
    public void exportDatabase(Context context, Uri uri) {
        // Replace with the actual database name
        String dbName = "AppDatabase.db";
        File dbFile = context.getDatabasePath(dbName);

        // Log the database file path to check if it's correct
        Log.d("DatabaseExport", "Database file path: " + dbFile.getAbsolutePath());

        // Check if the database file exists
        if (!dbFile.exists()) {
            Log.e("DatabaseExport", "Database file not found at: " + dbFile.getAbsolutePath());
            Toast.makeText(context, "Database file not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream input = new FileInputStream(dbFile);
             OutputStream output = context.getContentResolver().openOutputStream(uri)) {

            if (output == null) {
                Log.e("DatabaseExport", "Failed to open output stream for URI: " + uri);
                Toast.makeText(context, "Failed to open output stream.", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[1024];
            int length;

            // Read from the database and write to the output stream
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            Toast.makeText(context, "Database exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("DatabaseExport", "Error exporting database", e);
            Toast.makeText(context, "Error exporting database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void importDatabase(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            // Define the path where you want to import the database
            File dbFile = context.getDatabasePath(DATABASE_NAME);

            // Create an output stream to the database file
            OutputStream outputStream = new FileOutputStream(dbFile);

            // Buffer for data copying
            byte[] buffer = new byte[1024];
            int length;

            // Copy data from the input stream to the output stream
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Close the streams
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Toast.makeText(context, "Database imported successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error importing database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Additional methods for required supplies can be added here
}
