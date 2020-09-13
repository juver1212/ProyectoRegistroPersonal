package com.example.proyectoregistropersonal.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectoregistropersonal.R;
import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.utils.MyAdapter;
import com.example.proyectoregistropersonal.utils.MyAdapterMarcacionRegistros;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class listaFragment extends Fragment {

    private ListView listview;
    FloatingActionButton botonFlotante ,botonflotante2;

    public listaFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lista, container, false);

        DBManager dbManager = new DBManager(getActivity());
        dbManager.open();
        Cursor cursor = dbManager.ListarTrabajadores();

        botonFlotante = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        botonFlotante.show();
        botonflotante2 = (FloatingActionButton) getActivity().findViewById(R.id.fab2);
        botonflotante2.show();

        botonflotante2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment nuevoFragmento = new modificarTrabajadorFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, nuevoFragmento);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        listview = (ListView) v.findViewById(R.id.idlista);
        listview.setAdapter(new MyAdapter(getActivity(), android.R.layout.simple_list_item_1,
                cursor, new String[]{"Nombres"}, new int[]{android.R.id.text1}));
        listview.setFastScrollEnabled(true);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                final String nrodocumento = cursor.getString(1);
                final String Nombre = cursor.getString(2);

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.poput_lista_trabajadores, null);

                Button btnModificar = (Button) popupView.findViewById(R.id.btnModificarTrabajdor);
                Button btnEliminar = (Button) popupView.findViewById(R.id.btnEliminarTrabajdor);
                TextView NomTrabajador = (TextView) popupView.findViewById(R.id.txtNombre);

                NomTrabajador.setText(Nombre);

                int _width = LinearLayout.LayoutParams.MATCH_PARENT;
                int _height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, _width, _height, focusable);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


                btnEliminar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBManager dbManager = new DBManager(getActivity());
                        dbManager.open();
                        long result = dbManager.eliminarTrabajador(nrodocumento);
                        if(result > 0){
                            new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE).setTitleText("Excelente!").setContentText("Trabajador eliminado").show();
                        }else{
                            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE).setTitleText("Opss!").setContentText("Algo salio mal").show();
                        }

                        popupWindow.dismiss();

                        Fragment nuevoFragmento = new listaFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.nav_host_fragment, nuevoFragmento);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });

                btnModificar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        Bundle args = new Bundle();
                        args.putString("nroDocumento", nrodocumento);
                        Fragment nuevoFragmento = new modificarTrabajadorFragment();
                        nuevoFragmento.setArguments(args);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.nav_host_fragment, nuevoFragmento);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });

                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }
        });

        return v;
    }
}
