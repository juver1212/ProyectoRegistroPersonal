package com.example.proyectoregistropersonal.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.proyectoregistropersonal.R;
import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.utils.MyAdapter;
import com.example.proyectoregistropersonal.utils.MyAdapterMarcacionRegistros;
import com.example.proyectoregistropersonal.utils.MyAdapterRegistrarMarcacion;
import com.example.proyectoregistropersonal.utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class listaMarcacionFragment extends Fragment {

    private ListView listview;
    FloatingActionButton botonFlotante ,botonflotante2;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String nroDocumento = "", Nombres = "", latitude="", longitude="";
    private FusedLocationProviderClient fusedLocationClient;
    SweetAlertDialog pDialog;

    public listaMarcacionFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lista_marcacion, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        DBManager dbManager = new DBManager(getActivity());
        dbManager.open();
        Cursor cursor = dbManager.ListarTrabajadores();

        botonFlotante = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        botonFlotante.hide();
        botonflotante2 = (FloatingActionButton) getActivity().findViewById(R.id.fab2);
        botonflotante2.hide();

        listview = (ListView) v.findViewById(R.id.idlista);
        listview.setAdapter(new MyAdapterRegistrarMarcacion(getActivity(), android.R.layout.simple_list_item_1,
                cursor, new String[]{"Nombres"}, new int[]{android.R.id.text1}));
        listview.setFastScrollEnabled(true);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                nroDocumento = cursor.getString(1);
                Nombres = cursor.getString(2);

                if(cursor != null) {
                    pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
                    pDialog.setTitleText(Nombres);
                    pDialog.setContentText("¿Desea continuar con la marcación?");
                    pDialog.setConfirmText("Continuar");
                    pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                    });
                    pDialog.show();
                }
            }
        });


        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Logic to handle location object
                    latitude = String.valueOf(location.getLongitude());
                    longitude = String.valueOf(location.getLongitude());
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //Foto
            final DBManager dbManager = new DBManager(getActivity());
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            dbManager.open();
            ContentValues cv = new ContentValues();

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String fechaActual = df.format(c);

            Date dt = new Date();
            int hours = dt.getHours();
            int minutes = dt.getMinutes();
            int seconds = dt.getSeconds();
            String horaActual = hours + ":" + minutes + ":" + seconds;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imagen = baos.toByteArray();

            long result = 0;
            fusedLocationClient.getLastLocation();

            cv.put("CodEmpresa", "01");
            cv.put("Documento", nroDocumento);
            cv.put("Fecha", fechaActual);
            cv.put("Hora", horaActual);
            cv.put("Latitud", latitude);
            cv.put("Longitud", longitude);
            cv.put("Estado", "R");
            cv.put("Imagen", imagen);
            cv.put("UsuMod", "admin");
            cv.put("FecMod", fechaActual);

            Log.e("Latitud",latitude);
            Log.e("Longitude",longitude);

            result = dbManager.insertMarcacion(cv);
            if(result > 0){
                Utils.GuardarFotoCarpeta(imageBitmap, fechaActual , getActivity());
                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE).setTitleText(Nombres).setContentText("Marcación correcta").show();
                pDialog.dismiss();
                Fragment nuevoFragmento = new listaMarcacionFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, nuevoFragmento);
                transaction.addToBackStack(null);
                transaction.commit();
            }
            else{
                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE).setTitleText("Opss!").setContentText("Algo salio mal").show();
            }
        }
    }
}
