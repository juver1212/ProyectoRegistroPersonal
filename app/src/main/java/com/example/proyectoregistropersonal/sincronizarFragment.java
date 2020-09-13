package com.example.proyectoregistropersonal;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.proyectoregistropersonal.LoginActivity;
import com.example.proyectoregistropersonal.MainActivity;
import com.example.proyectoregistropersonal.R;
import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.fragment.listaMarcacionFragment;
import com.example.proyectoregistropersonal.fragment.modificarTrabajadorFragment;
import com.example.proyectoregistropersonal.utils.SesionActiva;
import com.example.proyectoregistropersonal.utils.httpConection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class sincronizarFragment extends Fragment {

    public sincronizarFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sincronizar, container, false);

        TextView text = v.findViewById(R.id.txtfechasincronizacion);
        Button boton = v.findViewById(R.id.btnsincronizar);
        Button boton2 = v.findViewById(R.id.btnLista);
        FloatingActionButton botonFlotante ,botonflotante2;

        botonFlotante = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        botonFlotante.hide();
        botonflotante2 = (FloatingActionButton) getActivity().findViewById(R.id.fab2);
        botonflotante2.hide();

        DBManager dbManager = new DBManager(getActivity());
        dbManager.open();
        Cursor cursor = dbManager.ListarRegistroSincronizaciones();

        if (cursor != null && cursor.getCount() > 0) {

            int hora_ = cursor.getColumnIndex("Hora");
            int fecha_ = cursor.getColumnIndex("Fecha");
            String hora = cursor.getString(hora_);
            String fecha = cursor.getString(fecha_);
            text.setText(fecha + " "+ hora);
        }

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new httpConection.SincronizarMarcaciones(getActivity()).execute();
            }
        });

        boton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                toolbar.setTitle("Registro de marcaciones");

                Fragment nuevoFragmento = new listaMarcacionFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, nuevoFragmento);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return v;
    }
}
