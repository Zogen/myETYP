package com.example.my;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AppDatabase.db";
    private static final int DATABASE_VERSION = 3;

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Pantry table
        String CREATE_PANTRY_TABLE = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT, " +
                COLUMN_PANTRY_QUANTITY + " INTEGER); ";
        db.execSQL(CREATE_PANTRY_TABLE);

        // Create Grocery table
        String CREATE_GROCERY_TABLE = "CREATE TABLE " + TABLE_GROCERY + " (" +
                COLUMN_GROCERY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GROCERY_NAME + " TEXT, " +
                COLUMN_GROCERY_QUANTITY + " INTEGER)";
        db.execSQL(CREATE_GROCERY_TABLE);

        // Create Transaction History table
        String CREATE_TRANSACTION_HISTORY_TABLE = "CREATE TABLE " + TABLE_TRANSACTION_HISTORY + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANSACTION_ITEM_NAME + " TEXT, " +
                COLUMN_TRANSACTION_QUANTITY + " INTEGER, " +
                COLUMN_TRANSACTION_DATE + " TEXT)";
        db.execSQL(CREATE_TRANSACTION_HISTORY_TABLE);
    }

    // Upgrade the database to add the new Transaction History table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <=2) {
            String CREATE_TRANSACTION_HISTORY_TABLE = "CREATE TABLE " + TABLE_TRANSACTION_HISTORY + " (" +
                    COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TRANSACTION_ITEM_NAME + " TEXT, " +
                    COLUMN_TRANSACTION_QUANTITY + " INTEGER, " +
                    COLUMN_TRANSACTION_DATE + " TEXT)";
            db.execSQL(CREATE_TRANSACTION_HISTORY_TABLE);
        }
    }

    // Insert a new item into the pantry
    // for use in the pantry activity.
    // This method is also used when we move the items
    // from the grocery list to the pantry.
    public void insertPantryItem(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_NAME, name);
        values.put(COLUMN_PANTRY_QUANTITY, quantity);
        db.insert(TABLE_PANTRY, null, values);
        db.close();
    }

    // Insert a new item into the grocery list
    public void insertGroceryItem(String name, int quantity) {
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
    public Cursor getAllGroceryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_GROCERY_ID + " AS _id, " + COLUMN_GROCERY_NAME + ", " + COLUMN_GROCERY_QUANTITY + " FROM " + TABLE_GROCERY;
        return db.rawQuery(query, null);
    }

    // Delete a pantry item (X button)
    public void deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, COLUMN_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete a grocery item (X button)
    public void deleteGroceryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROCERY, COLUMN_GROCERY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Look for an existing item in the Pantry Table
    // to avoid duplication during insertions
    public PantryItem getPantryItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "pantry",
                null,
                "name = ?",
                new String[]{name},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
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

    public void updateGroceryItem(EtypItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROCERY_NAME, item.getName());

        db.update(TABLE_GROCERY, values, COLUMN_GROCERY_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    // Retrieve grocery item by name, similar to pantry (see above)
    public EtypItem getGroceryItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "grocery",
                null,
                "name = ?",
                new String[]{name},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            cursor.close();
            return new EtypItem(id, name, quantity);
        }

        if (cursor != null) {
            cursor.close();
        }
        return null; // Item not found
    }

    // Update quantity of existing grocery item
    public void updateGroceryItemQuantity(int id, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", newQuantity);
        db.update("grocery", values, "id=?", new String[]{String.valueOf(id)});
    }

    // Get a list of items in Pantry table, for API call
    public List<String> getPantryItems() {
        List<String> pantryItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM pantry", null);

        if (cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
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

    // Get all transaction history records
    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_TRANSACTION_ID + " AS _id, " +
                COLUMN_TRANSACTION_ITEM_NAME + ", " +
                COLUMN_TRANSACTION_QUANTITY + ", " +
                COLUMN_TRANSACTION_DATE + " FROM " + TABLE_TRANSACTION_HISTORY;
        return db.rawQuery(query, null);
    }

}
