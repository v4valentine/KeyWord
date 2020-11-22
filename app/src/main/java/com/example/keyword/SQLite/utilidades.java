package com.example.keyword.SQLite;

public class utilidades {

    //Constantes campos
    public static final String TABLA_CONTACTOS = "contactos";
    public static final String CAMPO_ID = "id";
    public static final String TELEFONO = "telefono";


    public static final String CREAR_TABLA_CONTACTOS="CREATE TABLE "+TABLA_CONTACTOS+" ("+CAMPO_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+TELEFONO+" TEXT)";


}
