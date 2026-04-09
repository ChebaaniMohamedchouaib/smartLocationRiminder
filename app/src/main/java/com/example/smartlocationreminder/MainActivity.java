package com.example.smartlocationreminder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends AppCompatActivity {
    MapView map;
    Button btnAdd, btnView, btnLogout;
    LocationManager locationManager;
    DatabaseHelper myDb;
    String currentUser;
    boolean isDialogShowing = false;
    MyLocationNewOverlay mLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);

        // 1. ربط العناصر بملف الـ XML أولاً
        map = findViewById(R.id.mapView);
        btnAdd = findViewById(R.id.btnAddReminder);
        btnView = findViewById(R.id.btnViewReminders);
        btnLogout = findViewById(R.id.btnLogout);

        // 2. إنشاء طبقة تحديد الموقع (مهم جداً أن تكون هنا قبل تغيير الأيقونة)
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        mLocationOverlay.enableMyLocation();

        // 3. الآن يمكننا تغيير الأيقونة بأمان
        android.graphics.drawable.Drawable iconDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_custom_location);
        if (iconDrawable != null) { // حماية إضافية لتجنب أي خطأ
            android.graphics.Bitmap iconBitmap = android.graphics.Bitmap.createBitmap(
                    iconDrawable.getIntrinsicWidth(),
                    iconDrawable.getIntrinsicHeight(),
                    android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(iconBitmap);
            iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            iconDrawable.draw(canvas);

            mLocationOverlay.setPersonIcon(iconBitmap);
            mLocationOverlay.setDirectionIcon(iconBitmap);
            mLocationOverlay.setPersonHotspot(iconBitmap.getWidth() / 2.0f, iconBitmap.getHeight());
        }

        // 4. إضافة طبقة الموقع إلى الخريطة
        map.getOverlays().add(mLocationOverlay);

        // 5. إعداد الخريطة
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        // 6. باقي الإعدادات (قاعدة البيانات والمستخدم)
        myDb = new DatabaseHelper(this);
        currentUser = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "");

        createNotificationChannel();

        // 7. برمجة الأزرار
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddReminderActivity.class)));
        btnView.setOnClickListener(v -> startActivity(new Intent(this, ReminderListActivity.class)));
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkLocationPermission();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify", "Reminders", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            map.getController().animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()));
            checkReminders(location.getLatitude(), location.getLongitude());
        }
    };

    private void checkReminders(double lat, double lng) {
        // جلب تذكيرات المستخدم الحالي فقط
        Cursor cursor = myDb.getRemindersByUser(currentUser);
        while (cursor.moveToNext()) {
            String title = cursor.getString(1);
            double tLat = cursor.getDouble(2);
            double tLng = cursor.getDouble(3);

            float[] res = new float[1];
            Location.distanceBetween(lat, lng, tLat, tLng, res);
            if (res[0] < 50 && !isDialogShowing) {
                showNotification(title);
            }
        }
        cursor.close();
    }

    private void showNotification(String title) {
        isDialogShowing = true;
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, "notify")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("تذكير!")
                .setContentText("وصلت إلى: " + title)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(1, b.build());
        }

        new AlertDialog.Builder(this).setTitle("تذكير").setMessage(title)
                .setPositiveButton("حسناً", (d, w) -> isDialogShowing = false).show();
    }

    @Override public void onResume() { super.onResume(); map.onResume(); }
    @Override public void onPause() { super.onPause(); map.onPause(); }
}