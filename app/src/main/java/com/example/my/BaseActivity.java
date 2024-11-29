package com.example.my;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    // Called when the options menu is created. This adds items to the action bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Check if the current activity is EtypListActivity
        if (!(this instanceof EtypListActivity)) {
            // Remove the "Transaction History" menu item if not in EtypListActivity
            menu.findItem(R.id.action_transaction_history).setVisible(false);
        }
        return true;
    }

    // Handles item selection from the options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // If the "Settings" option is selected, start the SettingsActivity
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        // If the "Transaction History" option is selected, start the TransactionHistoryActivity
        else if (id == R.id.action_transaction_history) {
            Intent intent = new Intent(this, TransactionHistoryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item); // Handle other items by default
    }

}
