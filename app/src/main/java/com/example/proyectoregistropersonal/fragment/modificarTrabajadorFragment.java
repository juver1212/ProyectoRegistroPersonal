package com.example.proyectoregistropersonal.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.proyectoregistropersonal.R;
import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.database.DatabaseHelper;
import com.example.proyectoregistropersonal.utils.DatePickerFragment;
import com.example.proyectoregistropersonal.utils.MyToast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class modificarTrabajadorFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri imageUri;

    TextInputEditText etPlannedDate, etPlannedDateCese, txt_nroDocumento, txtNombres, txtSueldo, txtAsignacion, txtCci,
            txtNroCuenta, txtFechaInicio, txtFechaFin;
    Spinner spinnerCargo, spinnerBanco, spinnerCategoria, cboCentroCosto, cboEstados;
    String nroDocumento="";
    FloatingActionButton botonflotante ,botonflotante2;
    Button btnCancelar, btnActualizar;
    CircleImageView image, btnCargar, btnFoto;

    final Handler handler = new Handler();

    public modificarTrabajadorFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_modificar_trabajador, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        botonflotante = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        botonflotante.hide();
        botonflotante2 = (FloatingActionButton) getActivity().findViewById(R.id.fab2);
        botonflotante2.hide();

        final DBManager dbManager = new DBManager(getActivity());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nroDocumento = bundle.getString("nroDocumento");
        }

        spinnerCargo = (Spinner) v.findViewById(R.id.cboCargo);
        spinnerBanco = (Spinner) v.findViewById(R.id.cboBanco);
        spinnerCategoria = (Spinner) v.findViewById(R.id.cboCategoria);
        cboCentroCosto = (Spinner) v.findViewById(R.id.cboCentroCosto);
        cboEstados = (Spinner) v.findViewById(R.id.cboEstado);

        txt_nroDocumento = (TextInputEditText) v.findViewById(R.id.txt_nroDocumento);
        txtNombres = (TextInputEditText) v.findViewById(R.id.txtNombres);
        txtSueldo = (TextInputEditText) v.findViewById(R.id.txtSueldo);
        txtAsignacion = (TextInputEditText) v.findViewById(R.id.txtAsignacion);
        txtCci = (TextInputEditText) v.findViewById(R.id.txtCci);
        txtNroCuenta = (TextInputEditText) v.findViewById(R.id.txtNroCuenta);
        txtFechaInicio = (TextInputEditText) v.findViewById(R.id.etPlannedDate);
        txtFechaFin = (TextInputEditText) v.findViewById(R.id.etPlannedDateCese);

        btnActualizar = (Button) v.findViewById(R.id.btnActualizar);
        btnCancelar = (Button) v.findViewById(R.id.btnCancelar);
        etPlannedDate = (TextInputEditText) v.findViewById(R.id.etPlannedDate);
        etPlannedDateCese = (TextInputEditText) v.findViewById(R.id.etPlannedDateCese);

        image = (CircleImageView) v.findViewById(R.id.img);
        btnCargar = (CircleImageView) v.findViewById(R.id.btnCargar);
        btnFoto = (CircleImageView) v.findViewById(R.id.btnFoto);

        if(!nroDocumento.equals("")){
            toolbar.setTitle("Actualizar trabajador");
            txt_nroDocumento.setEnabled(false);
        }
        else{
            toolbar.setTitle("Registrar trabajador");
            txt_nroDocumento.setEnabled(true);
        }

        String[] cargos = {"OPERARIO", "OFICIAL MONTAJISTA", "OPERARIO ANDAMIERO", "RESIDENTE DE OBRA",
                "OFICIAL ESMERILADOR", "OFICIAL ANDAMIERO", "OPERARIO MONTAJISTA", "ASISTENTE DE CALIDAD", "RIGGER",
                "OPERARIO PINTOR", "SOLDADOR"};

        ArrayAdapter<CharSequence> langAdaptercargo = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_text, cargos);
        langAdaptercargo.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinnerCargo.setAdapter(langAdaptercargo);

        spinnerBanco = (Spinner) v.findViewById(R.id.cboBanco);
        final String[] bancos = {"BBVA", "BCP", "SCOTIABANK PERU", "INTERBANK"};
        ArrayAdapter<CharSequence> langAdapterbanco = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_text, bancos);
        langAdapterbanco.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinnerBanco.setAdapter(langAdapterbanco);

        spinnerCategoria = (Spinner) v.findViewById(R.id.cboCategoria);
        String[] categorias = {"DIRECTO","INDIRECTO"};
        ArrayAdapter<CharSequence> langAdaptercategoria = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_text, categorias);
        langAdaptercategoria.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinnerCategoria.setAdapter(langAdaptercategoria);

        cboCentroCosto = (Spinner) v.findViewById(R.id.cboCentroCosto);
        String[] costos = {"PACL-1101", "PACL-1170"};
        ArrayAdapter<CharSequence> langAdaptercostos = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_text, costos);
        langAdaptercostos.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        cboCentroCosto.setAdapter(langAdaptercostos);

        cboEstados = (Spinner) v.findViewById(R.id.cboEstado);
        String[] estados = {"ACTIVO", "INACTIVO"};
        ArrayAdapter<CharSequence> langAdapterestado = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_text, estados);
        langAdapterestado.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        cboEstados.setAdapter(langAdapterestado);

        etPlannedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.etPlannedDate:
                        showDatePickerDialog("ingreso");
                        break;
                }
            }
        });

        etPlannedDateCese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.etPlannedDateCese:
                        showDatePickerDialog("cese");
                        break;
                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        btnCargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment nuevoFragmento = new listaFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, nuevoFragmento);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbManager.open();
                ContentValues cv = new ContentValues();

                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String fechaActual = df.format(c);


                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] image = baos.toByteArray();

                if(txtNombres.getText().toString().equals("")) {
                    MyToast.showShort(getActivity(), "El nombre es obligatorio!");
                    return;
                }

                if(nroDocumento.equals("")) {
                    if(txt_nroDocumento.getText().toString().equals("")) {
                        MyToast.showShort(getActivity(), "El nro de documeto es obligatorio!");
                        return;
                    }
                    cv.put("Documento", txt_nroDocumento.getText().toString());
                }

                cv.put("Nombres", txtNombres.getText().toString());
                cv.put("Cargo", spinnerCargo.getSelectedItem().toString());
                cv.put("Sueldo", txtSueldo.getText().toString());
                cv.put("AsigFam", txtAsignacion.getText().toString());
                cv.put("EntidadFinanciera", spinnerBanco.getSelectedItem().toString());
                cv.put("CCI", txtCci.getText().toString());
                cv.put("NroCuenta", txtNroCuenta.getText().toString());
                cv.put("Categoria", spinnerCategoria.getSelectedItem().toString());
                cv.put("CentroCosto", cboCentroCosto.getSelectedItem().toString());
                cv.put("FechaIngreso", etPlannedDate.getText().toString());
                cv.put("FechaCese", etPlannedDateCese.getText().toString());
                cv.put("Estado", cboEstados.getSelectedItem().toString());
                cv.put("CodEmpresa", "03");
                cv.put("Foto", image);
                cv.put("UsuMod", "Usumod");
                cv.put("FecMod", fechaActual);

                int result = 0;
                if(nroDocumento.equals("")) {
                    result =  dbManager.insertarTrabajador(cv);
                }else{
                    result = dbManager.actualizarTrabajador(cv, txt_nroDocumento.getText().toString());
                }

                if(result > 0){
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE).setTitleText("Excelente!").setContentText("La operación se realizo satisfactoriamente").show();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Fragment nuevoFragmento = new listaFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.nav_host_fragment, nuevoFragmento);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }, 2000);
                }
                else{
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE).setTitleText("Opss!").setContentText("Algo salio mal").show();
                }
            }
        });

        dbManager.open();
        Cursor cursor = dbManager.ObtenerTrabajador(nroDocumento);
        if(cursor!=null && cursor.getCount()>0) {
            int Nombre_ = cursor.getColumnIndex(DatabaseHelper.Nombres);
            int Documento_ = cursor.getColumnIndex(DatabaseHelper.Documento);
            int Sueldo_ = cursor.getColumnIndex(DatabaseHelper.Sueldo);
            int AsigFam_ = cursor.getColumnIndex(DatabaseHelper.AsigFam);
            int CCI_ = cursor.getColumnIndex(DatabaseHelper.CCI);
            int NroCuenta_ = cursor.getColumnIndex(DatabaseHelper.NroCuenta);
            int EntidadFinanciera_ = cursor.getColumnIndex(DatabaseHelper.EntidadFinanciera);
            int Cargo_ = cursor.getColumnIndex(DatabaseHelper.Cargo);
            int Categoria_ = cursor.getColumnIndex(DatabaseHelper.Categoria);
            int CentroCosto_ = cursor.getColumnIndex(DatabaseHelper.CentroCosto);
            int Estado_ = cursor.getColumnIndex(DatabaseHelper.Estado);
            int FechaInicio_ = cursor.getColumnIndex(DatabaseHelper.FechaIngreso);
            int FechaFin_ = cursor.getColumnIndex(DatabaseHelper.FechaCese);
            int Foto_ = cursor.getColumnIndex(DatabaseHelper.Foto);

            String Nombre = cursor.getString(Nombre_);
            String Documento = cursor.getString(Documento_);
            String Sueldo = cursor.getString(Sueldo_);
            String AsigFam = cursor.getString(AsigFam_);
            String CCI = cursor.getString(CCI_);
            String NroCuenta = cursor.getString(NroCuenta_);
            String EntidadFinanciera = cursor.getString(EntidadFinanciera_);
            String Cargo = cursor.getString(Cargo_);
            String Categoria = cursor.getString(Categoria_);
            String CentroCosto = cursor.getString(CentroCosto_);
            String Estado = cursor.getString(Estado_);
            String FechaInicio = cursor.getString(FechaInicio_);
            String FechaFin = cursor.getString(FechaFin_);
            byte[] blob = cursor.getBlob(Foto_);

            Bitmap bit;
            if (blob == null || blob.toString().equals("")) {
                bit = BitmapFactory.decodeResource(v.getResources(), R.drawable.usuario);
            } else {
                ByteArrayInputStream bais = new ByteArrayInputStream(blob);
                bit = BitmapFactory.decodeStream(bais);
            }


            txt_nroDocumento.setText(Documento);
            txtNombres.setText(Nombre);
            txtSueldo.setText(Sueldo);
            txtAsignacion.setText(AsigFam);
            txtCci.setText(CCI);
            txtNroCuenta.setText(NroCuenta);
            spinnerCargo.setSelection(langAdaptercargo.getPosition(Cargo));
            spinnerBanco.setSelection(langAdapterbanco.getPosition(EntidadFinanciera));
            spinnerCategoria.setSelection(langAdaptercategoria.getPosition(Categoria));
            cboCentroCosto.setSelection(langAdaptercostos.getPosition(CentroCosto));
            cboEstados.setSelection(langAdapterestado.getPosition(Estado));
            txtFechaInicio.setText(FechaInicio);
            txtFechaFin.setText(FechaFin);
            image.setImageBitmap(bit);
        }

        return  v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(imageBitmap);
        }
        if(resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            image.setImageURI(imageUri);
        }
    }

    private void showDatePickerDialog(String valor) {
        final String tipo = valor;
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                if(tipo.equals("ingreso")) {
                    final String selectedDate = day + " / " + (month + 1) + " / " + year;
                    etPlannedDate.setText(selectedDate);
                }else{
                    final String selectedDate = day + " / " + (month + 1) + " / " + year;
                    etPlannedDateCese.setText(selectedDate);
                }
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }



}
