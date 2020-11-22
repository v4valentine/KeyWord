package com.example.keyword.ui.contactos;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keyword.R;
import com.example.keyword.SQLite.ConexionSQLiteHelper;
import com.example.keyword.SQLite.utilidades;

import java.util.ArrayList;

public class adaptador extends RecyclerView.Adapter<adaptador.ViewHolderDatos>{

    static ArrayList<modelo> telefonos;
    static Context context;
    static Activity activity;

    private void bd_eliminarContacto(String id){

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(context, utilidades.TABLA_CONTACTOS,null,1);

        if(context != null){
            SQLiteDatabase db = conn.getWritableDatabase();

            String[] parameters = {id};

            db.delete(utilidades.TABLA_CONTACTOS,utilidades.CAMPO_ID+"=?",parameters);

            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+utilidades.TABLA_CONTACTOS+"'");
        }
    }

    public adaptador(ArrayList<modelo> telefonos, boolean sn,Context context,Activity activity){

        this.telefonos = telefonos;
        adaptador.activity = activity;
        adaptador.context = context;
    }

    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacto,null,false);

        return new ViewHolderDatos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderDatos holder, final int position) {

        holder.id.setText(telefonos.get(position).getId());
        holder.telefono.setText(telefonos.get(position).getTelefono());
        holder.eliminar_tel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    int nPosicion = holder.getAdapterPosition();
                    bd_eliminarContacto(holder.id.getText().toString());
                    telefonos.remove(nPosicion);
                    adaptador.this.notifyItemRemoved(nPosicion);
                    notifyItemRangeChanged(nPosicion,telefonos.size());

            }
        });

    }

    @Override
    public int getItemCount() {

        return telefonos.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {

        TextView id;
        TextView telefono;
        Button eliminar_tel;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);

            id = (TextView) itemView.findViewById(R.id.id);
            telefono = (TextView) itemView.findViewById(R.id.telefono);
            eliminar_tel = (Button) itemView.findViewById(R.id.eliminarTelefono);

        }

    }

}
