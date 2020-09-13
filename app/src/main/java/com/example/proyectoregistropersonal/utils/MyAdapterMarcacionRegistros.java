package com.example.proyectoregistropersonal.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.proyectoregistropersonal.R;
import com.example.proyectoregistropersonal.database.DatabaseHelper;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MyAdapterMarcacionRegistros extends CursorAdapter {

    public MyAdapterMarcacionRegistros(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_lista_marcacion, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        int Nombre_ = cursor.getColumnIndex("conteo");
        int Hora_ = cursor.getColumnIndex("Hora");

        String Nombre = cursor.getString(Nombre_);
        String Hora = cursor.getString(Hora_);

        TextView txtNombre = (TextView) view.findViewById(R.id.txtConteo);
        TextView txtHora = (TextView) view.findViewById(R.id.txtHora);

        txtNombre.setText(Nombre);
        txtHora.setText(Hora);

        int pos = cursor.getPosition();
        txtNombre.setTag(pos);
        txtHora.setTag(pos);
    }

}