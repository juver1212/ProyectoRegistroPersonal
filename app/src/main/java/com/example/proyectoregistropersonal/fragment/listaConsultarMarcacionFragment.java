package com.example.proyectoregistropersonal.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.proyectoregistropersonal.R;
import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.utils.DatePickerFragment;
import com.example.proyectoregistropersonal.utils.MyAdapter;
import com.example.proyectoregistropersonal.utils.MyAdapterMarcacion;
import com.example.proyectoregistropersonal.utils.MyAdapterMarcacionRegistros;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class listaConsultarMarcacionFragment extends Fragment {

    private ListView listview;
    FloatingActionButton botonFlotante ,botonflotante2;
    TextInputEditText  etPlannedDate;

    public listaConsultarMarcacionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_consultar_marcacion, container, false);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String fechaActual = df.format(c);

        DBManager dbManager = new DBManager(getActivity());
        dbManager.open();
        final Cursor cursor = dbManager.ListarMarcaciones(fechaActual);

        botonFlotante = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        botonFlotante.hide();
        botonflotante2 = (FloatingActionButton) getActivity().findViewById(R.id.fab2);
        botonflotante2.hide();

        etPlannedDate = (TextInputEditText) v.findViewById(R.id.etPlannedDate);
        listview = (ListView) v.findViewById(R.id.idlista);
        listview.setAdapter(new MyAdapterMarcacion(getActivity(), android.R.layout.simple_list_item_1,
                cursor, new String[]{"Nombres"}, new int[]{android.R.id.text1}));
        listview.setFastScrollEnabled(true);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.poput_consulta_marcacion, null);

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if (cursor != null) {

                    Button btnCancelar = (Button) popupView.findViewById(R.id.btnCancelar);
                    ListView lista = (ListView) popupView.findViewById(R.id.idlista);

                    DBManager dbManager = new DBManager(getActivity());
                    dbManager.open();
                    String documento = cursor.getString(1);
                    Cursor cursorRegistros = dbManager.ListarMarcacionesEntidad(fechaActual, documento);

                    MyAdapterMarcacionRegistros todoAdapter = new MyAdapterMarcacionRegistros(getActivity(), cursorRegistros);
                    lista.setAdapter(todoAdapter);


                    int _width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int _height = LinearLayout.LayoutParams.MATCH_PARENT;
                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, _width, _height, focusable);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


                    btnCancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    popupView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popupWindow.dismiss();
                            return true;
                        }
                    });
                }else{
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE).setTitleText("Sin registros!").setContentText("La entidad seleccionada no cuenta con registros de marcación").show();
                }


            }
        });

        etPlannedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.etPlannedDate:
                        showDatePickerDialog();
                        break;
                }
            }
        });
        return v;
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                final String selectedDate = day + " / " + (month + 1) + " / " + year;
                etPlannedDate.setText(selectedDate);

                String mes = String.valueOf(month + 1);
                String day_ = String.valueOf(day);

                String mesNew = "00".substring(mes.length()) + mes;
                String dayNew = "00".substring(day_.length()) + day_;
                String fecha_sql = String.valueOf(year)+'-'+mesNew+"-"+dayNew;
                Log.e("Fecha consulta", fecha_sql);
                DBManager dbManager = new DBManager(getActivity());
                dbManager.open();
                Cursor cursor = dbManager.ListarMarcaciones(fecha_sql);
                listview.setAdapter(new MyAdapterMarcacion(getActivity(), android.R.layout.simple_list_item_1,
                        cursor, new String[]{"Nombres"}, new int[]{android.R.id.text1}));
                listview.setFastScrollEnabled(true);
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }
}
