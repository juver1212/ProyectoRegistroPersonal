package com.example.proyectoregistropersonal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.database.DatabaseHelper;
import com.example.proyectoregistropersonal.utils.SesionActiva;
import com.example.proyectoregistropersonal.utils.Utils;
import com.example.proyectoregistropersonal.utils.httpConection;

import java.io.File;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

    DBManager dbManager;
    Context c;
    Spinner spinner;

    static {
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl"
        );
    }

    Button btncargar, btningresar;
    TextView txtUsuario, txtClave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        VerificarPermisos();

        c = this;

        try {
            Utils.BD_backup();
            Log.e("RESPALD GENRADO","Generado backup");
        }catch ( IOException e){
            Log.e("ERRORRRR","Error al generar backup");
        }

        final Context c = this;
        spinner = (Spinner) findViewById(R.id.spinner1);
        btncargar = (Button) findViewById(R.id.btCargar);
        btningresar = (Button) findViewById(R.id.btLogin);
        txtUsuario = (TextView) findViewById(R.id.username);
        txtClave = (TextView) findViewById(R.id.password);

        dbManager = new DBManager(c);
        dbManager.open();
        final Cursor cursor = dbManager.ObtenerEmpresas();
        startManagingCursor(cursor);

        String[] columns = new String[] { "DesEmpresa" };
        int[] to = new int[] { android.R.id.text1 };

        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, columns, to);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);


        btncargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new httpConection.SincronizarUsuarios(c).execute();
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        startActivity(getIntent());
                    }
                }, 7000);
            }
        });

        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                String empresa = ((Cursor) spinner.getSelectedItem()).getString(1);*/
                if(cursor!=null && cursor.getCount()>0) {
                    String usuario = txtUsuario.getText().toString();
                    String clave = txtClave.getText().toString();
                    String empresa = ((Cursor) spinner.getSelectedItem()).getString(1);
                    dbManager = new DBManager(c);
                    dbManager.open();
                    Cursor cursor = dbManager.ObtenerUsuario(usuario, clave, empresa);
                    if (cursor != null && cursor.getCount() > 0) {

                        int usu_ = cursor.getColumnIndex("NomUsuario");
                        int pass_ = cursor.getColumnIndex("ClaveUsuario");
                        int codemp_ = cursor.getColumnIndex("CodEmpresa");
                        int desemp_ = cursor.getColumnIndex("DesEmpresa");

                        String usu = cursor.getString(usu_);
                        String pass = cursor.getString(pass_);
                        String codemp = cursor.getString(codemp_);
                        String desemp = cursor.getString(desemp_);

                        ((SesionActiva) getApplication()).setUsuario(usu);
                        ((SesionActiva) getApplication()).setContrasena(pass);
                        ((SesionActiva) getApplication()).setEmpresaCod(codemp);
                        ((SesionActiva) getApplication()).setEmpresaDes(desemp);
                        Intent inten = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(inten);
                        LoginActivity.this.finish();
                    } else {
                        new SweetAlertDialog(c, SweetAlertDialog.ERROR_TYPE).setTitleText("Datos incorrectos!").setContentText("El usuario o contraseña son incorrectos").show();
                    }
                }else{
                    new SweetAlertDialog(c, SweetAlertDialog.ERROR_TYPE).setTitleText("Primero Sincronizar!").setContentText("Es necesario sincronizar los datos antes de continuar").show();
                }
            }
        });
    }

    private void VerificarPermisos() {
        try {
            int permsRequestCode = 100;
            String[] perms = {Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

            int telefono = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            int escritura = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int lectura = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int camara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
            int gps = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);



            if (telefono == PackageManager.PERMISSION_GRANTED && escritura == PackageManager.PERMISSION_GRANTED &&
                    camara == PackageManager.PERMISSION_GRANTED && lectura == PackageManager.PERMISSION_GRANTED &&
                    internet == PackageManager.PERMISSION_GRANTED && gps == PackageManager.PERMISSION_GRANTED) {
                    //Cumple con los permisos
            } else {
                ActivityCompat.requestPermissions(this, perms, permsRequestCode);
            }
        }
        catch (Exception e){

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean indica = true;

        switch (requestCode){
            case 100:
                for(int i = 0; i < grantResults.length; i++)
                {
                    if (grantResults[i] != 0){
                        indica = false;
                    }
                }
                if(indica)
                {
                    //Cumple con los permisos
                } else {
                    mostrarMensaje();
                }
                break;
        }
    }

    private void mostrarMensaje() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos incompletos")
                .setMessage("Se necesita la aprobación de los permisos para poder continuar.")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                })
                .show();
    }

    public void CargarDatosCombo(){
        final Cursor cursor = dbManager.ObtenerEmpresas();
        startManagingCursor(cursor);
        String[] columns = new String[] { "DesEmpresa" };
        int[] to = new int[] { android.R.id.text1 };

        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(c, android.R.layout.simple_spinner_item, cursor, columns, to);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);
    }
}