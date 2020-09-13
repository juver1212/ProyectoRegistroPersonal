package com.example.proyectoregistropersonal;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.fragment.listaConsultarMarcacionFragment;
import com.example.proyectoregistropersonal.fragment.listaFragment;
import com.example.proyectoregistropersonal.fragment.listaMarcacionFragment;
import com.example.proyectoregistropersonal.utils.SesionActiva;
import com.example.proyectoregistropersonal.utils.Utils;
import com.example.proyectoregistropersonal.utils.httpConection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String[] nombre_campos = {"Documento", "Nombres","Cargo","Sueldo","AsigFam","EntidadFinanciera","CCI",
            "NroCuenta","Categoria","CentroCosto","FechaIngreso","FechaCese","Estado","CodEmpresa","UsuReg","FecReg"};
    final Handler handler = new Handler();
    private DBManager dbManager;
    private AppBarConfiguration mAppBarConfiguration;
    NavigationView navigationView;
    DrawerLayout drawer;
    SweetAlertDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                buscarArchivo();
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_lista_trabajador, R.id.nav_registro_marcacion, R.id.nav_consulta_marcacion, R.id.nav_sincronizar_marcacion, R.id.nav_optimizar_espacio)
                .setDrawerLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setItemIconTintList(null);
        View header = navigationView.getHeaderView(0);

        String usuario = ((SesionActiva) this.getApplication()).getUsuario();
        String empresa = ((SesionActiva) this.getApplication()).getEmpresaDes();

        TextView titulo = (TextView) header.findViewById(R.id.nomUsuario);
        TextView subtitulo = (TextView) header.findViewById(R.id.nomEmpresa);
        titulo.setText(usuario);
        subtitulo.setText(empresa);

        toolbar.setTitle("Lista de trabajadores");
        FragmentManager manager1 = getSupportFragmentManager();
        listaFragment nuevoFragment = new listaFragment();
        manager1.beginTransaction().replace(R.id.nav_host_fragment, nuevoFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    public void buscarArchivo() {
        Intent txtIntent ;
        txtIntent = new Intent();
        txtIntent .setAction( Intent.ACTION_GET_CONTENT );
        txtIntent .setType( "*/*" );
        startActivityForResult( Intent.createChooser( txtIntent , "DEMO" ), 1001 );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Log.i("URL", "Cancelado por el usuario");
        }
        if ((resultCode == RESULT_OK) && (requestCode == 1001)) {
            String path1 = data.getData().getPath();

            Uri uri = data.getData();
            File file = new File(uri.getPath());//create path from uri
            final String[] split = file.getPath().split(":");//split the path.
            path1 = split[1];//assign it to a string(your choice).

            leerExcel(path1);
        }
    }

    public void leerExcel(String rutaArchivo){

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String fechaActual = df.format(c);

        dbManager = new DBManager(this);
        dbManager.open();
        ContentValues cv = new ContentValues();
        dbManager.deleteAll();

        if(!rutaArchivo.contains("/storage/emulated/0/")){
            rutaArchivo = "/storage/emulated/0/"+rutaArchivo;
        }

        Log.e("URL", rutaArchivo);
        //File file1 = new File("/storage/emulated/0/Download/excel.xlsx");
        File file1 = new File(rutaArchivo);

        try (FileInputStream file = new FileInputStream(file1)) {
            // leer archivo excel
            XSSFWorkbook worbook = new XSSFWorkbook(file);
            //obtener la hoja que se va leer
            XSSFSheet sheet = worbook.getSheetAt(0);
            //obtener todas las filas de la hoja excel
            Iterator<Row> rowIterator = sheet.iterator();

            Row row;

            long result=0;
            // se recorre cada fila hasta el final
            int ind_filas = 0;
            while (rowIterator.hasNext()) {
                Log.e("Indica nueva fila", "--------------");
                row = rowIterator.next();
                //se obtiene las celdas por fila
                Iterator<Cell> cellIterator = row.cellIterator();
                Cell cell;

                //se recorre cada celda
                if(ind_filas > 0) {
                    int ind_campos = 0;
                    while (cellIterator.hasNext()) {
                        DataFormatter formatter = new DataFormatter();
                        // se obtiene la celda en específico y se la imprime
                        cell = cellIterator.next();
                        String val = formatter.formatCellValue(cell);

                        if (dbManager != null) {
                            if(nombre_campos[ind_campos].equals("UsuReg")){
                                cv.put(nombre_campos[ind_campos], "admin");
                                Log.e(nombre_campos[ind_campos], "admin");
                            }
                            if(nombre_campos[ind_campos].equals("FecReg")){
                                cv.put(nombre_campos[ind_campos], fechaActual);
                                Log.e(nombre_campos[ind_campos], fechaActual);
                            }
                            else {
                                cv.put(nombre_campos[ind_campos], val);
                                Log.e(nombre_campos[ind_campos], val);
                            }
                        }
                        ind_campos++;
                    }
                    result = dbManager.insertTrabajador(cv);
                    cv = new ContentValues();
                    Log.e("DATABASE","Insertado correctamente");
                }
                ind_filas++;
            }

            if(result > 0) {
                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE).setTitleText("Excelente!").setContentText("Datos registrados").show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        startActivity(getIntent());
                    }
                }, 2000);
            }
            else{
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("Opss!").setContentText("Algo salio mal").show();
            }

            Log.e("OHHHH",String.valueOf(result));
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (id == R.id.nav_lista_trabajador) {
            toolbar.setTitle("Lista de trabajadores");
            FragmentManager manager = getSupportFragmentManager();
            listaFragment fragment = new listaFragment();
            manager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }else if(id == R.id.nav_registro_marcacion){
            toolbar.setTitle("Registro de marcaciones");
            FragmentManager manager = getSupportFragmentManager();
            listaMarcacionFragment fragment = new listaMarcacionFragment();
            manager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }
        else if(id == R.id.nav_consulta_marcacion){
            toolbar.setTitle("Consulta de marcaciones");
            FragmentManager manager = getSupportFragmentManager();
            listaConsultarMarcacionFragment fragment = new listaConsultarMarcacionFragment();
            manager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }
        else if(id == R.id.nav_sincronizar_marcacion){
            toolbar.setTitle("Sincronizar marcaciones");
            FragmentManager manager = getSupportFragmentManager();
            sincronizarFragment fragment = new sincronizarFragment();
            manager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }
        else if(id == R.id.nav_optimizar_espacio){
            final Context c = this;
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Optimizar espacio")
                    .setContentText("¿Esta seguro de borrar los datos almacenados?")
                    .setConfirmText("Ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            try {
                                Utils.EliminarFotoCarpeta(c);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sDialog.dismissWithAnimation();
                            FragmentManager manager1 = getSupportFragmentManager();
                            listaFragment nuevoFragment = new listaFragment();
                            manager1.beginTransaction().replace(R.id.nav_host_fragment, nuevoFragment).commit();
                        }
                    }).show();
        }
        else if(id == R.id.nav_cerrar_sesion){
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
            pDialog.setTitleText("Cerrar sesión");
            pDialog.setContentText("¿Desea cerrar la sesión?");
            pDialog.setConfirmText("Cerrar");
            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    Intent intent = new Intent (getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, 0);
                    finish();
                }
            });
            pDialog.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
