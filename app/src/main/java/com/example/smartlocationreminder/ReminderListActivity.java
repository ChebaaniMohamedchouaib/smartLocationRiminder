package com.example.smartlocationreminder;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ReminderListActivity extends AppCompatActivity {
    ListView listView;
    DatabaseHelper myDb;
    ArrayList<String> items, ids;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        listView = findViewById(R.id.listViewReminders);
        myDb = new DatabaseHelper(this);
        items = new ArrayList<>(); ids = new ArrayList<>();

        loadData();

        listView.setOnItemLongClickListener((p, v, pos, id) -> {
            new AlertDialog.Builder(this).setMessage("حذف؟").setPositiveButton("نعم", (d, w) -> {
                myDb.deleteReminder(ids.get(pos));
                loadData();
            }).show();
            return true;
        });
        // تفعيل زر الرجوع
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        String user = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "");
        Cursor c = myDb.getRemindersByUser(user);
        items.clear(); ids.clear();
        while (c.moveToNext()) {
            ids.add(c.getString(0));
            items.add(c.getString(1));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }
}