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
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.proyectoregistropersonal.R;
import com.example.proyectoregistropersonal.database.DBManager;
import com.example.proyectoregistropersonal.database.DatabaseHelper;

import org.apache.poi.sl.usermodel.Line;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MyAdapter extends SimpleCursorAdapter implements SectionIndexer {

    Context context;
    LayoutInflater cursorInflater;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_COUNT = 2;
    private AlphabetIndexer indexer;
    private int[] usedSectionNumbers;
    private Map<Integer, Integer> sectionToOffset;
    private Map<Integer, Integer> sectionToPosition;

    public MyAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);

        cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;

        indexer = new AlphabetIndexer(c, c.getColumnIndexOrThrow("Nombres"), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        sectionToPosition = new TreeMap<Integer, Integer>(); //use a TreeMap because we are going to iterate over its keys in sorted order
        sectionToOffset = new HashMap<Integer, Integer>();

        final int count = super.getCount();

        int i;
        // tener temporalmente una sección del alfabeto del mapa para el primer índice que aparece
        // (este mapa va a hacer algo más más adelante)
        for (i = count - 1 ; i >= 0; i--){
            sectionToPosition.put(indexer.getSectionForPosition(i), i);
        }

        i = 0;
        usedSectionNumbers = new int[sectionToPosition.keySet().size()];

        // tenga en cuenta que para cada sección que aparece antes de una posición, debemos compensar nuestro
        // índices por 1, para dejar espacio para un encabezado alfabético en nuestra lista
        for (Integer section : sectionToPosition.keySet()){
            sectionToOffset.put(section, i);
            usedSectionNumbers[i] = section;
            i++;
        }

        // use offset para asignar las secciones del alfabeto a sus índices reales en la lista
        for(Integer section: sectionToPosition.keySet()){
            sectionToPosition.put(section, sectionToPosition.get(section) + sectionToOffset.get(section));
        }
    }

    @Override
    public int getCount() {
        if (super.getCount() != 0){
            // a veces su conjunto de datos se invalida. En este caso getCount ()
            // debería devolver 0 y no nuestro recuento ajustado para los encabezados.
            // La única forma de saber si los datos están invalidados es comprobar si
            //super.getCount () es 0.
            return super.getCount() + usedSectionNumbers.length;
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (getItemViewType(position) == TYPE_NORMAL){
            // definimos esta función en el código completo más tarde
            // si el elemento de la lista no es un encabezado, buscamos el elemento del conjunto de datos con la misma posición
            // compensado por el número de encabezados que aparecen antes del elemento en la lista
            return super.getItem(position - sectionToOffset.get(getSectionForPosition(position)) - 1);
        }

        return null;
    }

    @Override
    public int getPositionForSection(int section) {
        if (! sectionToOffset.containsKey(section)){
            // Este es solo el caso cuando FastScroller se está desplazando,
            // por lo que esta sección no aparece en nuestro conjunto de datos. La implementación
            // de Fastscroller requiere que las secciones faltantes tengan el mismo índice que el
            // comienzo de la siguiente sección que no falta (o el final de la lista si
            // si faltan el resto de las secciones).
            // Entonces, en el ejemplo pictórico, las secciones D y E aparecerían en la posición 9
            // y G a Z aparecen en la posición 11.
            int i = 0;
            int maxLength = usedSectionNumbers.length;

            // escaneo lineal sobre las secciones (número constante de estas) que aparecen en el
            // conjunto de datos para encontrar la primera sección utilizada que sea mayor que la sección dada, por lo que en el
            // ejemplo D y E corresponden a F
            while (i < maxLength && section > usedSectionNumbers[i]){
                i++;
            }
            if (i == maxLength) return getCount(); // la sección dada es más allá de todos nuestros datos

            return indexer.getPositionForSection(usedSectionNumbers[i]) + sectionToOffset.get(usedSectionNumbers[i]);
        }

        return indexer.getPositionForSection(section) + sectionToOffset.get(section);
    }



    @Override
    public int getSectionForPosition(int position) {
        int i = 0;
        int maxLength = usedSectionNumbers.length;

        // exploración lineal sobre las posiciones de las secciones alfabéticas utilizadas
        // para encontrar dónde encaja la sección dada
        while (i < maxLength && position >= sectionToPosition.get(usedSectionNumbers[i])){
            i++;
        }
        return usedSectionNumbers[i-1];
    }


    @Override
    public Object[] getSections() {
        return indexer.getSections();
    }
    // nada más que esto: los encabezados tienen posiciones que administra sectionIndexer.
    @Override
    public int getItemViewType(int position) {
        if (position == getPositionForSection(getSectionForPosition(position))){
            return TYPE_HEADER;
        } return TYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    // devuelve la vista del encabezado, si está en una posición de encabezado de sección
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);
        if (type == TYPE_HEADER){
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.header, parent, false);
            }
            ((TextView)convertView.findViewById(R.id.header)).setText((String)getSections()[getSectionForPosition(position)]);
            return convertView;
        }
        return super.getView(position - sectionToOffset.get(getSectionForPosition(position)) - 1, convertView, parent);
    }


    //these two methods just disable the headers
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItemViewType(position) == TYPE_HEADER){
            return false;
        }
        return true;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View viewAdapter = LayoutInflater.from(context).inflate(R.layout.item_lista_trabajador, parent, false);
        return viewAdapter;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        int Nombre_ = cursor.getColumnIndex(DatabaseHelper.Nombres);
        int Documento_ = cursor.getColumnIndex(DatabaseHelper.Documento);
        int Foto_ = cursor.getColumnIndex(DatabaseHelper.Foto);

        String Nombre = cursor.getString(Nombre_);
        String Documento = cursor.getString(Documento_);

        byte[] blob = cursor.getBlob(Foto_);

        Bitmap bit;

        if(blob == null || blob.toString().equals("")) {
            bit = BitmapFactory.decodeResource(view.getResources(), R.drawable.usuario);
            Log.e("VALOR","Entroooo");
        }else{
            Log.e("VALOR","No Entroooo");
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            bit = BitmapFactory.decodeStream(bais);
        }

        TextView txtNombre = (TextView) view.findViewById(R.id.txtNombre);
        TextView txtDocumento = (TextView) view.findViewById(R.id.txtDocumento);
        ImageView image = (ImageView) view.findViewById(R.id.profile_image);

        txtNombre.setText(Nombre);
        txtDocumento.setText(Documento);
        image.setImageBitmap(bit);


        int pos = cursor.getPosition();
        txtNombre.setTag(pos);
        txtDocumento.setTag(pos);
        image.setTag(pos);
    }

}