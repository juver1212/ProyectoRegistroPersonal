package com.example.proyectoregistropersonal.database;
/**
 * Created by anupamchugh on 19/10/15.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }


    public long insertTrabajador(ContentValues contentValue) {
        long  i = database.insert(DatabaseHelper.TABLE_NAME_TRABAJADOR, null, contentValue);
        return i;
    }

    public long eliminarTrabajador(String documento) {
        long  i = database.delete("trabajadores", "Documento = '"+documento+"'", null);
        return i;
    }

    public Cursor ListarTrabajadores() {
        String[] columns = new String[] { DatabaseHelper.Nombres, DatabaseHelper.Documento, DatabaseHelper.FechaIngreso };
        String where = "IsEnabled=1";
        //Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, where, null, null, null, null);
        Cursor cursor =  database.rawQuery( "select t.Documento as _id,*, (select Fecha||' '||Hora from trabajadormarcacion tm where tm.Documento = t.Documento order by tm.id desc limit 1) as ultimaMarcacion from "+ DatabaseHelper.TABLE_NAME_TRABAJADOR+" t order by Nombres", null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor ObtenerTrabajador(String documento) {
        Cursor cursor =  database.rawQuery( "select Documento as _id,* from "+DatabaseHelper.TABLE_NAME_TRABAJADOR+" where Documento = '"+documento+"'", null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public int actualizarTrabajador(ContentValues _contentValue, String documento) {
        ContentValues contentValues = _contentValue;
        int i = database.update(DatabaseHelper.TABLE_NAME_TRABAJADOR, contentValues, DatabaseHelper.Documento + " = '" + documento+"'", null);
        return i;
    }

    public int insertarTrabajador(ContentValues _contentValue) {
        ContentValues contentValues = _contentValue;
        int i = Integer.parseInt(String.valueOf(database.insert(DatabaseHelper.TABLE_NAME_TRABAJADOR, null, contentValues)));
        return i;
    }
/*
    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }*/

    public void deleteAll() {
        database.delete(DatabaseHelper.TABLE_NAME_TRABAJADOR, null, null);
    }

    public long insertMarcacion(ContentValues contentValue) {
        long  i = database.insert(DatabaseHelper.TABLE_NAME_MARCACION, null, contentValue);
        return i;
    }

    public long insertRegistroSincronizacion(ContentValues contentValue) {
        long  i = database.insert(DatabaseHelper.TABLE_NAME_SINCRONIZAR_REGISTRO, null, contentValue);
        return i;
    }


    public Cursor ListarMarcaciones(String fecha) {
        String MY_QUERY = "SELECT t.Documento as _id, t.Documento, Nombres, t.Foto, count(m.Documento) as conteo FROM trabajadores t LEFT JOIN " +
                "trabajadormarcacion m ON t.Documento = m.Documento " +
                "where DATE(substr(m.Fecha,7,4)||'-'||substr(m.Fecha,4,2)||'-'||substr(m.Fecha,1,2)) = DATE(?) group by Nombres, Foto order by Nombres";

        String MY_QUERY2 = "SELECT t.Documento as _id, t.Documento, Nombres, t.Foto, " +
                "(select count(m.Documento) from trabajadormarcacion m where " +
                "DATE(substr(m.Fecha,7,4)||'-'||substr(m.Fecha,4,2)||'-'||substr(m.Fecha,1,2)) = DATE(?) " +
                "and m.Documento = t.Documento) as conteo FROM trabajadores t order by Nombres";

        Cursor cursor =  database.rawQuery( MY_QUERY2, new String[]{String.valueOf(fecha)});
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor ListarRegistroSincronizaciones() {
        String MY_QUERY2 = "SELECT * FROM sincronizacion_registro t order by id desc limit 1";

        Cursor cursor =  database.rawQuery( MY_QUERY2, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor ListarMarcacionesEntidad(String fecha, String entidad) {

        String MY_QUERY2 = "select id as _id, (select count(*) from trabajadormarcacion b  where b.id >= m.id) as conteo, Hora from trabajadormarcacion m where " +
                "DATE(substr(m.Fecha,7,4)||'-'||substr(m.Fecha,4,2)||'-'||substr(m.Fecha,1,2)) = DATE(?) " +
                "and m.Documento = ? order by _id desc";

        Cursor cursor =  database.rawQuery( MY_QUERY2, new String[]{String.valueOf(fecha), String.valueOf(entidad)});
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor ListarMarcaciones() {
        String MY_QUERY2 = "select id as _id, * from trabajadormarcacion m where estado <> 'E' order by _id desc";
        Cursor cursor =  database.rawQuery( MY_QUERY2, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void cctualizarMarcacion(int id) {
        String MY_QUERY2 = "update trabajadormarcacion set estado = 'E' where id = "+id;
        database.rawQuery( MY_QUERY2, null);
    }

    public long insertUsuario(ContentValues contentValue) {
        long  i = database.insert("usuario", null, contentValue);
        return i;
    }

    public void deleteAllUsuario() {
        database.delete("usuario", null, null);
    }

    public Cursor ObtenerUsuario(String usuario, String clave, String empresa) {
        Cursor cursor =  database.rawQuery( "select Documento as _id,* from usuario where USU_CODIGO = '"+usuario+"' and ClaveUsuario = '"+clave+"' and DesEmpresa ='"+empresa+"'", null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor ObtenerEmpresas() {
        Cursor cursor =  database.rawQuery( "select distinct DesEmpresa as _id, DesEmpresa from usuario", null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}
