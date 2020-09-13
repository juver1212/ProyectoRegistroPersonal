package com.example.proyectoregistropersonal.utils;

import android.app.Application;

public class SesionActiva extends Application {


    private String Usuario = "";
    private String Contrasena = "";
    private String EmpresaDes = "";
    private String EmpresaCod = "";

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public String getContrasena() {
        return Contrasena;
    }

    public void setContrasena(String contrasena) {
        Contrasena = contrasena;
    }

    public String getEmpresaDes() {
        return EmpresaDes;
    }

    public void setEmpresaDes(String empresaDes) {
        EmpresaDes = empresaDes;
    }

    public String getEmpresaCod() {
        return EmpresaCod;
    }

    public void setEmpresaCod(String empresaCod) {
        EmpresaCod = empresaCod;
    }

}
