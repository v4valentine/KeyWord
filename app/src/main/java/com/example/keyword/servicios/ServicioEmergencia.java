package com.example.keyword.servicios;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.keyword.R;
import com.example.keyword.SQLite.ConexionSQLiteHelper;
import com.example.keyword.SQLite.utilidades;
import com.example.keyword.ui.contactos.FragmentContactos;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.keyword.ui.contactos.FragmentContactos.CHANEL_ID;

public class ServicioEmergencia extends Service{

    private BluetoothAdapter bluetoothAdapter;
    private Location location;
    private Boolean pulsera;
    public static boolean LIFE;
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        notification();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LIFE = true;

        pulsera = false;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter.startDiscovery()) {

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);

        }

    }

    private void notification(){

        Intent notificationIntent = new Intent(getApplicationContext(), FragmentContactos.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new NotificationCompat.Builder(this, CHANEL_ID)
                    .setContentTitle(getText(R.string.titulo))
                    .setContentText(getText(R.string.mensaje))
                    .setSmallIcon(R.drawable.ic_baseline_settings_remote_24)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
        }

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)){

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceHardwareAddress = device.getAddress();

                if (deviceHardwareAddress.equals("00:18:E4:35:1C:74")) {

                    pulsera = true;

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions

                        return;
                    }
                    String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);


                    do {

                        if ((provider.contains("gps")) & (location == null)) {

                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        }

                        if ((provider.contains("network")) & (location == null)) {

                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }

                        if(location == null){

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                    }while (location == null);
                }

                if ((pulsera) & (location != null)) {

                    enviarMensaje();

                }

            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                bluetoothAdapter.startDiscovery();

            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetoothAdapter.cancelDiscovery();

        unregisterReceiver(receiver);

        LIFE = false;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void ubicacion(String numero) {

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        List<String> providerList = locationManager.getAllProviders();

        if (null != location && null != providerList && providerList.size() > 0) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    String _Location = listAddresses.get(0).getAddressLine(0);

                    sms(numero,"¡Ayuda!, me encuentro en " + _Location + "... (mensaje generado por la app de seguridad Keyword)");

                    if(pulsera){

                        pulsera = false;

                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    public void sms(String numero, String mensaje) {

        SmsManager mySmsManager = SmsManager.getDefault();
        mySmsManager.sendTextMessage(numero, null, mensaje, null, null);



    }

    private void enviarMensaje(){

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getBaseContext(), utilidades.TABLA_CONTACTOS, null, 1);

        SQLiteDatabase db = conn.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + utilidades.TABLA_CONTACTOS, null);

        while (c.moveToNext()) {

            ubicacion(c.getString(1));

        }

        c.close();

        db.close();

        location = null;

    }
}
