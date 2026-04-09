package com.example.smartlocationreminder;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class AddReminderActivity extends AppCompatActivity {
    MapView map;
    EditText etTitle;
    Button btnSave;
    DatabaseHelper myDb;
    double sLat, sLng;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_add_reminder);

        myDb = new DatabaseHelper(this);
        etTitle = findViewById(R.id.etTitle);
        btnSave = findViewById(R.id.btnSave);
        map = findViewById(R.id.mapView);

        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(34.8516, 5.7281));

        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                sLat = p.getLatitude(); sLng = p.getLongitude();
                if (marker != null) map.getOverlays().remove(marker);
                marker = new Marker(map);
                marker.setPosition(p);
                map.getOverlays().add(marker);
                map.invalidate();
                return true;
            }
            @Override public boolean longPressHelper(GeoPoint p) { return false; }
        }));

        btnSave.setOnClickListener(v -> {
            String user = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "");
            if (myDb.insertReminder(etTitle.getText().toString(), sLat, sLng, user)) {
                Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}