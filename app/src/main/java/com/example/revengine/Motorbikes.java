package com.example.revengine;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Motorbikes extends AppCompatActivity {
    private ListView listViewMotorbikes;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motorbikes);

        listViewMotorbikes = findViewById(R.id.list_view_products);
        databaseHelper = new DatabaseHelper(this);

        displayMotorbikes();
    }

    private void displayMotorbikes() {
        Cursor cursor = databaseHelper.getMotorbikes200HPAndAbove();

        if (cursor != null && cursor.getCount() > 0) {
            ProductAdapter adapter = new ProductAdapter(this, cursor, 0);
            listViewMotorbikes.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No motorbikes found with 200 HP and above", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}