package com.example.keyword.ui.contactos;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keyword.R;
import com.example.keyword.SQLite.ConexionSQLiteHelper;
import com.example.keyword.SQLite.utilidades;
import com.example.keyword.servicios.ServicioEmergencia;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getSystemService;

public class FragmentContactos extends Fragment{

    private HomeViewModel homeViewModel;
    ArrayList<modelo> telefonos;
    RecyclerView recyclerView;
    private FloatingActionButton add;
    ConexionSQLiteHelper conn;
    adaptador ca;
    private ImageView rastreo;
    public Button guardar,cancelar;
    private EditText numero_tel;
    static boolean encendida;
    private final static int REQUEST_ENABLE_BT = 1;
    public static final String CHANEL_ID = "servicioEmergencia";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        encendida = false;

        createNotificationChanel();

    }

    private void createNotificationChanel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel serviceChannel = new NotificationChannel(

                    CHANEL_ID,"Keyword", NotificationManager.IMPORTANCE_DEFAULT

            );

            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

        }

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        telefonos = new ArrayList<>();

        conn = new ConexionSQLiteHelper(getContext(),utilidades.TABLA_CONTACTOS,null,1);

        add = (FloatingActionButton) root.findViewById(R.id.add);

        rastreo = (ImageView) root.findViewById(R.id.rastreo);

        recyclerView = (RecyclerView) root.findViewById(R.id.rv_lista_contactos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        consultarListaPersonas();

        ca = new adaptador(telefonos,false,getContext(), getActivity());
        recyclerView.setAdapter(ca);

        rastreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        getActivity(),Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(getActivity(),new String[]{

                            Manifest.permission.SEND_SMS,
                            Manifest.permission.SMS_FINANCIAL_TRANSACTIONS

                    },1000);


                }else{

                    if(encendida)apagar();

                    else {

                        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
                        Intent enablebt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enablebt,REQUEST_ENABLE_BT);

                        encerder();}

                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View DialogView = LayoutInflater.from(getActivity()).inflate(R.layout.sol_telefono,null);;
                numero_tel = (EditText) DialogView.findViewById(R.id.numero_tel);
                guardar =(Button) DialogView.findViewById(R.id.Guardar);
                cancelar = (Button) DialogView.findViewById(R.id.Cancelar);

                AlertDialog.Builder add = new AlertDialog.Builder(getActivity());

                add.setCancelable(true).setView(DialogView);

                final Dialog dialog = add.create();

                guardar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick( View view) {

                        if(!numero_tel.getText().toString().isEmpty()){

                            kingCrimson();

                        }

                        dialog.cancel();

                    }
                });

                cancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        dialog.cancel();

                    }
                });


                dialog.show();
            }
        });


        if(ServicioEmergencia.LIFE){

            encender_icon();

        }

        return root;
    }


    private void encerder(){

        encender_icon();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(getActivity(), ServicioEmergencia.class);
                getActivity().startService(i);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getActivity().startForegroundService(i);
                }


            }
        }).start();

    }

    public void encender_icon(){
        Toast.makeText(getContext(),"Con seguridad",Toast.LENGTH_SHORT).show();
        rastreo.setImageResource(R.drawable.logo_4);
        rastreo.setBackgroundColor(Color.parseColor("#822129"));
        ImageViewCompat.setImageTintList(rastreo, ColorStateList.valueOf(Color.parseColor("#ffffff")));

        encendida = true;
    }


    private void apagar() {

        Toast.makeText(getContext(),"Sin seguridad",Toast.LENGTH_SHORT).show();
        rastreo.setImageResource(R.drawable.logo_5);
        rastreo.setBackgroundColor(Color.parseColor("#CFCFCF"));
        ImageViewCompat.setImageTintList(rastreo, ColorStateList.valueOf(Color.parseColor("#000000")));

        Intent i = new Intent(getActivity(), ServicioEmergencia.class);
        getActivity().stopService(i);

        encendida = false;

    }

    private String bd_findId(String telefono){

        String r_id = "";

        try {

            ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getContext(),utilidades.TABLA_CONTACTOS,null,1);

            SQLiteDatabase db = conn.getReadableDatabase();

            String[] b_id = {telefono.toString()};
            String[] id = {utilidades.CAMPO_ID};

            Cursor c = db.query(utilidades.TABLA_CONTACTOS,id,utilidades.CAMPO_ID+"=?",b_id,null,null,null);

            c.moveToFirst();

            r_id = c.getString(0).toString();
            c.close();

        }catch (Exception e){}

        if(r_id.isEmpty()){

            r_id = String.valueOf(1+telefonos.size());

        }
        return r_id;

    }

    private void bd_ingresarContacto(String telefono){

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getContext(),utilidades.TABLA_CONTACTOS,null,1);

        SQLiteDatabase db = conn.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(utilidades.TELEFONO,telefono);

        long telResult = db.insert(utilidades.TABLA_CONTACTOS,utilidades.TELEFONO,values);

        db.close();
    }

    private void consultarListaPersonas(){

        SQLiteDatabase db = conn.getReadableDatabase();

        modelo mod = null;

        Cursor c = db.rawQuery("SELECT * FROM "+ utilidades.TABLA_CONTACTOS,null);

        while (c.moveToNext()){

            mod = new modelo();
            mod.setId(c.getString(0));
            mod.setTelefono(c.getString(1));

            telefonos.add(mod);

        }

        c.close();

        db.close();
    }

    private void kingCrimson(){

        try{
                if(telefonos.isEmpty()){

                    nuevoContacto();

                }else {

                    boolean b = true;

                    for (modelo m:telefonos) {
                        if(m.getTelefono().equals(numero_tel.getText().toString())){

                            Toast.makeText(getContext(),"Este número ya esta registrado",Toast.LENGTH_LONG).show();

                            b = false;

                            break;
                        }
                    }

                    if(b == true){

                        nuevoContacto();
                    }
                }

        }catch(Exception e){

            System.out.println(e);

        }

    }

    private void nuevoContacto(){

        telefonos.add(new modelo(numero_tel.getText().toString(),bd_findId(numero_tel.getText().toString())));

        bd_ingresarContacto(numero_tel.getText().toString());

        ca.notifyDataSetChanged();
    }

}