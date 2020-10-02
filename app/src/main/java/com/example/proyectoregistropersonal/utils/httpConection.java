package com.example.proyectoregistropersonal.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyectoregistropersonal.LoginActivity;
import com.example.proyectoregistropersonal.MainActivity;
import com.example.proyectoregistropersonal.database.DBManager;
import com.google.gson.Gson;

import org.apache.poi.xdgf.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class httpConection {

    public static class SincronizarUsuarios extends AsyncTask<String, String, String> {
        private WeakReference<Context> contextRef;

        SweetAlertDialog pDialog;
        HttpURLConnection conn;
        URL url_new = null;
        ArrayList<String> datos = new ArrayList<String>();

        public SincronizarUsuarios(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(contextRef.get(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#05647F"));
            pDialog.setTitleText("Sincronizando datos...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Utils.ConexionSSL();
                url_new = new URL("https://www.innovationtechnologyperu.com/asistencia/index.php/usuario_login/fillallusuarioAndroid");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception";
            }
            try {
                conn = (HttpURLConnection) url_new.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("pass", "c2FsdmFkb3IgdmVnYSByZXllcw==anVuaW9yIHZlZ2EgcmV5ZXM=");
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                int response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return (result.toString());

                } else {
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();

            Gson gson = new Gson();
            JSONArray obj = null;
            try {
                obj = new JSONArray(result);
                if (obj.length() > 0) {

                    int resultado = 0;
                    final DBManager dbManager = new DBManager(contextRef.get());
                    dbManager.open();
                    dbManager.deleteAllUsuario();
                    ContentValues cv;

                    for (int x = 0; x < obj.length(); x++) {
                        JSONObject dato = obj.getJSONObject(x);
                        String IDUsuario = dato.getString("IDUsuario");
                        String USU_CODIGO = dato.getString("USU_CODIGO");
                        String CodEmpresa = dato.getString("CodEmpresa");
                        String DesEmpresa = dato.getString("DesEmpresa");
                        String NomUsuario = dato.getString("NomUsuario");
                        String ClaveUsuario = dato.getString("ClaveUsuario");
                        String Documento = dato.getString("Documento");
                        String TipoUsuario = dato.getString("TipoUsuario");

                        cv = new ContentValues();

                        cv.put("IDUsuario", IDUsuario);
                        cv.put("USU_CODIGO", USU_CODIGO);
                        cv.put("CodEmpresa", CodEmpresa);
                        cv.put("DesEmpresa", DesEmpresa);
                        cv.put("NomUsuario", NomUsuario);
                        cv.put("ClaveUsuario", ClaveUsuario);
                        cv.put("Documento", Documento);
                        cv.put("TipoUsuario", TipoUsuario);
                        try {
                            dbManager.insertUsuario(cv);
                            resultado++;
                        } catch (Exception e) {
                            resultado = 0;
                            new SweetAlertDialog(contextRef.get(), SweetAlertDialog.ERROR_TYPE).setTitleText("Algo salio mal!").setContentText("Se registraron errores al insertar datos").show();
                            return;
                        }

                    }
                    if (resultado > 0) {
                        new SweetAlertDialog(contextRef.get(), SweetAlertDialog.SUCCESS_TYPE).setTitleText("Excelente!").setContentText("Datos sincronizados").show();
                    }
                } else {
                    new SweetAlertDialog(contextRef.get(), SweetAlertDialog.WARNING_TYPE).setTitleText("Sin datos!").setContentText("No se encontraron datos para sincronizar").show();
                    Vibrator v = (Vibrator) contextRef.get().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SincronizarMarcaciones extends AsyncTask<String, String, String> {
        private WeakReference<Context> contextRef;

        SweetAlertDialog pDialog;
        HttpURLConnection conn;
        URL url_new = null;
        Cursor cursor;
        DBManager dbManager;

        public SincronizarMarcaciones(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(contextRef.get(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#05647F"));
            pDialog.setTitleText("Sincronizando marcaciones...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            dbManager = new DBManager(contextRef.get());
            dbManager.open();
            cursor = dbManager.ListarMarcaciones();
            JSONArray listaJson = new JSONArray();
            JSONObject objetoJson;
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    objetoJson = new JSONObject();
                    try {
                        String dia = cursor.getString(cursor.getColumnIndex("Fecha")).substring(0,2);
                        String mes = cursor.getString(cursor.getColumnIndex("Fecha")).substring(3,5);
                        String anio = cursor.getString(cursor.getColumnIndex("Fecha")).substring(6,10);

                        objetoJson.put("CodEmpresa", cursor.getString(cursor.getColumnIndex("CodEmpresa")));
                        objetoJson.put("Documento", cursor.getString(cursor.getColumnIndex("Documento")));
                        objetoJson.put("Fecha", anio+"-"+mes+"-"+dia);
                        objetoJson.put("Hora", cursor.getString(cursor.getColumnIndex("Hora")));
                        objetoJson.put("Latitud", cursor.getString(cursor.getColumnIndex("Latitud")));
                        objetoJson.put("Longitud", cursor.getString(cursor.getColumnIndex("Longitud")));
                        String imagen = "";
                        byte[] blob = cursor.getBlob(cursor.getColumnIndex("Imagen"));
                        try {
                            imagen = new String(blob, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        objetoJson.put("Imagen", imagen);
                        objetoJson.put("UsuReg", cursor.getString(cursor.getColumnIndex("UsuMod")));
                        listaJson.put(objetoJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cursor.moveToNext();
                }
            }

            try {
                Utils.ConexionSSL();
                url_new = new URL("https://www.innovationtechnologyperu.com/asistencia/index.php/sincroniza/SyncMarcacion");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception";
            }
            try {
                conn = (HttpURLConnection) url_new.openConnection();
                conn.setReadTimeout(180000);
                conn.setConnectTimeout(180000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_clave", "hola")
                        .appendQueryParameter("vp_lst", listaJson.toString());
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                int response_code = conn.getResponseCode();
                String reponse = conn.getResponseMessage();

                Log.e("respuesta codigo ", String.valueOf(response_code));
                Log.e("respuesta", reponse);

                if (response_code == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return (result.toString());

                } else {
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("LOG",e.getMessage());
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            Log.e("RESPUESTAAAA", result);
            JSONObject objetoJson;

            try {
                objetoJson = new JSONObject(result);
                String respuesta = objetoJson.getString("status");
                if (respuesta.equals("0")) {
                    if (cursor != null && cursor.getCount() > 0) {
                        /* cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            int id = cursor.getInt(cursor.getColumnIndex("_id"));
                            dbManager.cctualizarMarcacion(id);
                            cursor.moveToNext();
                        }

                        Date c = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                        String fechaActual = df.format(c);

                        Date dt = new Date();
                        int hours = dt.getHours();
                        int minutes = dt.getMinutes();
                        int seconds = dt.getSeconds();
                        String horaActual = hours + ":" + minutes + ":" + seconds;

                        ContentValues cv = new ContentValues();
                        cv.put("Fecha", fechaActual);
                        cv.put("Hora", horaActual);
                        dbManager.insertRegistroSincronizacion(cv);*/
                        Log.e("LLEGOO CORRECTO","TODO CORRECTO POR AQUI");
                        new SincronizarTrabajadores(contextRef.get(), cursor).execute();
                        //new SweetAlertDialog(contextRef.get(), SweetAlertDialog.SUCCESS_TYPE).setTitleText("Excelente!").setContentText("Datos sincronizados").show();
                    }
                } else {
                    new SweetAlertDialog(contextRef.get(), SweetAlertDialog.ERROR_TYPE).setTitleText("Opss!").setContentText("Ocurrio un problema al sincronizar las marcaciones").show();
                    Vibrator v = (Vibrator) contextRef.get().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SincronizarTrabajadores extends AsyncTask<String, String, String> {
        private WeakReference<Context> contextRef;
        private WeakReference<Cursor> cursorMarcaciones;

        SweetAlertDialog pDialog;
        HttpURLConnection conn;
        URL url_new = null;
        Cursor cursor;
        Cursor cursorMarcacion;
        DBManager dbManager;

        public SincronizarTrabajadores(Context context, Cursor _cursorMarcaciones) {
            contextRef = new WeakReference<>(context);
            cursorMarcaciones = new WeakReference<>(_cursorMarcaciones);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("LLEGO TRABAJADOR","LLEGOOOO");
            pDialog = new SweetAlertDialog(contextRef.get(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#05647F"));
            pDialog.setTitleText("Sincronizando trabajadores...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.e("LLEGO TRABAJADOR 2","LLEGOOOO");
            cursorMarcacion = cursorMarcaciones.get();
            dbManager = new DBManager(contextRef.get());
            dbManager.open();
            cursor = dbManager.ListarTrabajadores();
            JSONArray listaJson = new JSONArray();
            JSONObject objetoJson;
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    objetoJson = new JSONObject();
                    try {

                        String dia_Ing = "01";
                        String mes_Ing = "01";
                        String anio_Ing = "1999";
                        String fechaI = cursor.getString(cursor.getColumnIndex("FechaIngreso"));
                        Log.e("LLEGO TRABAJADOR 2.5","LLEGOOOO");
                        if(!fechaI.equals("") && fechaI.length() > 7) {
                            Log.e("LLEGO TRABAJADOR 2.5","LLEGOOOO");
                            Log.e("ES FECHA",fechaI);
                            dia_Ing = cursor.getString(cursor.getColumnIndex("FechaIngreso")).substring(0,2);
                            mes_Ing = cursor.getString(cursor.getColumnIndex("FechaIngreso")).substring(3,5);
                            if(fechaI.length() == 8) {
                                anio_Ing = "20"+cursor.getString(cursor.getColumnIndex("FechaIngreso")).substring(6,8);
                            }else{
                                anio_Ing = cursor.getString(cursor.getColumnIndex("FechaIngreso")).substring(6,10);
                            }
                        }
                        Log.e("LLEGO TRABAJADOR 3","LLEGOOOO");
                        String dia_Ces = "01";
                        String mes_Ces = "01";
                        String anio_Ces = "1999";
                        String fechaC = cursor.getString(cursor.getColumnIndex("FechaCese"));

                        if(!fechaC.equals("") && fechaC.length() > 7) {
                            Log.e("LLEGO TRABAJADOR 3.5","LLEGOOOO");
                            Log.e("ES FECHA OTRO",fechaC);
                             dia_Ces = cursor.getString(cursor.getColumnIndex("FechaCese")).substring(0, 2);
                             mes_Ces = cursor.getString(cursor.getColumnIndex("FechaCese")).substring(3, 5);
                            if(fechaC.length() == 8) {
                                anio_Ing = "20"+cursor.getString(cursor.getColumnIndex("FechaCese")).substring(6, 8);
                            }else{
                                anio_Ing = cursor.getString(cursor.getColumnIndex("FechaCese")).substring(6, 10);
                            }
                        }
                        Log.e("LLEGO TRABAJADOR 4","LLEGOOOO");
                        //objetoJson.put("CodEmpresa", cursor.getString(cursor.getColumnIndex("_id")));
                        //objetoJson.put("CodEmpresa", cursor.getString(cursor.getColumnIndex("CodEmpresa")));

                        if(String.valueOf(cursor.getString(cursor.getColumnIndex("CodEmpresa"))).equals("") ||
                                String.valueOf(cursor.getString(cursor.getColumnIndex("CodEmpresa"))).equals("null")){
                            objetoJson.put("CodEmpresa", "01");
                        }
                        else{
                            objetoJson.put("CodEmpresa", cursor.getString(cursor.getColumnIndex("CodEmpresa")));
                        }

                        objetoJson.put("Documento", cursor.getString(cursor.getColumnIndex("Documento")));
                        objetoJson.put("Nombres", cursor.getString(cursor.getColumnIndex("Nombres")));
                        objetoJson.put("Cargo", cursor.getString(cursor.getColumnIndex("Cargo")));
                        objetoJson.put("Sueldo", cursor.getString(cursor.getColumnIndex("Sueldo")));
                        objetoJson.put("AsigFam", cursor.getString(cursor.getColumnIndex("AsigFam")));
                        objetoJson.put("EntidadFinanciera", cursor.getString(cursor.getColumnIndex("EntidadFinanciera")));
                        objetoJson.put("CCI", cursor.getString(cursor.getColumnIndex("CCI")));
                        objetoJson.put("NroCuenta", cursor.getString(cursor.getColumnIndex("NroCuenta")));
                        objetoJson.put("Categoria", cursor.getString(cursor.getColumnIndex("Categoria")));
                        objetoJson.put("CentroCosto", cursor.getString(cursor.getColumnIndex("CentroCosto")));
                        objetoJson.put("FechaIngreso", anio_Ing+"-"+mes_Ing+"-"+dia_Ing);
                        objetoJson.put("FechaCese", anio_Ces+"-"+mes_Ces+"-"+dia_Ces);
                        String imagen = "";
                        byte[] blob = cursor.getBlob(cursor.getColumnIndex("Foto"));
                        try {
                            if(blob != null) {
                                Log.e("BLOG",blob.toString());
                                imagen = new String(blob, "UTF-8");
                            }else{
                                imagen = "";
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        objetoJson.put("Foto", imagen);

                        if(String.valueOf(cursor.getString(cursor.getColumnIndex("UsuReg"))).equals("") ||
                                String.valueOf(cursor.getString(cursor.getColumnIndex("CodEmpresa"))).equals("null")){
                            objetoJson.put("UsuReg", "admin");
                        }
                        else{
                            objetoJson.put("UsuReg", cursor.getString(cursor.getColumnIndex("UsuReg")));
                        }

                        listaJson.put(objetoJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cursor.moveToNext();
                }
            }

            try {
                Utils.ConexionSSL();
                url_new = new URL("https://www.innovationtechnologyperu.com/asistencia/index.php/sincroniza/SyncTrabajador");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception";
            }
            try {
                conn = (HttpURLConnection) url_new.openConnection();
                conn.setReadTimeout(180000);
                conn.setConnectTimeout(180000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_clave", "hola")
                        .appendQueryParameter("vp_lst", listaJson.toString());
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                int response_code = conn.getResponseCode();
                String reponse = conn.getResponseMessage();

                Log.e("respuesta codigo trabajador", String.valueOf(response_code));
                Log.e("respuesta trabajador", reponse);

                if (response_code == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return (result.toString());

                } else {
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("LOG",e.getMessage());
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            Log.e("RESPUESTAAAA TRABAJDOR", result);
            JSONObject objetoJson;

            try {
                objetoJson = new JSONObject(result);
                String respuesta = objetoJson.getString("status");
                if (respuesta.equals("1")) {
                    if (cursorMarcacion != null && cursorMarcacion.getCount() > 0) {
                        cursorMarcacion.moveToFirst();
                        while (!cursorMarcacion.isAfterLast()) {
                            int id = cursorMarcacion.getInt(cursorMarcacion.getColumnIndex("_id"));
                            dbManager.cctualizarMarcacion(id);
                            cursorMarcacion.moveToNext();
                        }

                        Date c = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                        String fechaActual = df.format(c);

                        Date dt = new Date();
                        int hours = dt.getHours();
                        int minutes = dt.getMinutes();
                        int seconds = dt.getSeconds();
                        String horaActual = hours + ":" + minutes + ":" + seconds;

                        ContentValues cv = new ContentValues();
                        cv.put("Fecha", fechaActual);
                        cv.put("Hora", horaActual);
                        dbManager.insertRegistroSincronizacion(cv);

                        new SweetAlertDialog(contextRef.get(), SweetAlertDialog.SUCCESS_TYPE).setTitleText("Excelente!").setContentText("Datos sincronizados").show();
                    }
                } else {
                    new SweetAlertDialog(contextRef.get(), SweetAlertDialog.ERROR_TYPE).setTitleText("Opss!").setContentText("Ocurrio un problema al sincronizar las marcaciones").show();
                    Vibrator v = (Vibrator) contextRef.get().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }
            } catch (JSONException e) {
                new SweetAlertDialog(contextRef.get(), SweetAlertDialog.ERROR_TYPE).setTitleText("Opss!").setContentText("Error al parsear respuesta").show();
                Vibrator v = (Vibrator) contextRef.get().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(600);
                e.printStackTrace();
            }
        }
    }
}