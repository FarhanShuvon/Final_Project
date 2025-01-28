package com.example.revengine;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Cars extends AppCompatActivity {
    private ListView listViewCars;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        listViewCars = findViewById(R.id.list_view_products);
        databaseHelper = new DatabaseHelper(this);

        displayCars();
    }

    private void displayCars() {
        Cursor cursor = databaseHelper.getCarsUnder200HP();

        if (cursor != null && cursor.getCount() > 0) {
            ProductAdapter adapter = new ProductAdapter(this, cursor, 0);
            listViewCars.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No cars found under 200 HP", Toast.LENGTH_SHORT).show();
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