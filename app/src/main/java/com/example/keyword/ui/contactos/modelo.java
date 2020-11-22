package com.example.keyword.ui.contactos;

public class modelo {

    String id;

    String telefono;

    public modelo(){}

    public modelo(String telefono) {
        this.telefono = telefono;
    }

    public modelo(String telefono, String id){this.id = id;
    this.telefono = telefono;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
