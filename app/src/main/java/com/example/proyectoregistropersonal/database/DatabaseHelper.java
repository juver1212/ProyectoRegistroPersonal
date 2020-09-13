package com.example.proyectoregistropersonal.database;

/**
 * Created by anupamchugh on 19/10/15.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME_TRABAJADOR = "trabajadores";
    public static final String TABLE_NAME_MARCACION = "trabajadormarcacion";
    public static final String TABLE_NAME_USUARIO = "usuario";
    public static final String TABLE_NAME_SINCRONIZAR_REGISTRO = "sincronizacion_registro";

    // Table columns

    public static final String Documento = "Documento";
    public static final String Nombres = "Nombres";
    public static final String Cargo = "Cargo";
    public static final String Sueldo = "Sueldo";
    public static final String AsigFam = "AsigFam";
    public static final String EntidadFinanciera = "EntidadFinanciera";
    public static final String CCI = "CCI";
    public static final String NroCuenta = "NroCuenta";
    public static final String Categoria = "Categoria";
    public static final String CentroCosto = "CentroCosto";
    public static final String FechaIngreso = "FechaIngreso";
    public static final String FechaCese = "FechaCese";
    public static final String Estado = "Estado";
    public static final String CodEmpresa = "CodEmpresa";
    public static final String Foto = "Foto";
    public static final String UsuReg = "UsuReg";
    public static final String FecReg = "FecReg";
    public static final String UsuMod = "UsuMod";
    public static final String FecMod = "FecMod";


    public static final String Latitud = "Latitud";
    public static final String Longitud = "Longitud";
    public static final String Fecha = "Fecha";
    public static final String Hora = "Hora";
    public static final String Imagen = "Imagen";


    // Database Information
    public static final String DB_NAME = "REGISTRO.DB";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME_TRABAJADOR + "("
            + Documento + " TEXT, " + Nombres + " TEXT , " + Cargo + " TEXT, "
            + Sueldo + " REAL, " + AsigFam + " REAL , " + EntidadFinanciera + " TEXT, "
            + CCI + " TEXT, " + NroCuenta + " TEXT , " + Categoria + " TEXT, "
            + CentroCosto + " TEXT, " + FechaIngreso + " TEXT , " + FechaCese + " TEXT, "
            + Estado + " TEXT, " + CodEmpresa + " TEXT , " + Foto + " BLOB, "
            + UsuReg + " TEXT, " + FecReg + " TEXT ,"
            + UsuMod + " TEXT, " + FecMod + " TEXT );";

    private static final String CREATE_TABLE_MARCACION = "create table " + TABLE_NAME_MARCACION + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT ," + CodEmpresa + " TEXT, " + Documento + " TEXT , " + Fecha + " TEXT, " + Hora + " TEXT, "
            + Latitud + " TEXT, " + Longitud + " TEXT , " + Estado + " TEXT, "
            + Imagen + " BLOB, " + UsuMod + " TEXT , " + FecMod + " TEXT );";

    private static final String CREATE_TABLE_USUARIO = "create table " + TABLE_NAME_USUARIO + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, IDUsuario TEXT, USU_CODIGO TEXT, CodEmpresa TEXT, DesEmpresa TEXT," +
            "NomUsuario TEXT, ClaveUsuario TEXT, Documento TEXT, TipoUsuario TEXT );";

    private static final String CREATE_TABLE_REGISTRO_SINCRONIZACION = "create table " + TABLE_NAME_SINCRONIZAR_REGISTRO + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, Fecha TEXT, Hora TEXT );";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_MARCACION);
        db.execSQL(CREATE_TABLE_USUARIO);
        db.execSQL(CREATE_TABLE_REGISTRO_SINCRONIZACION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TRABAJADOR);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MARCACION);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_USUARIO);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SINCRONIZAR_REGISTRO);
        onCreate(db);
    }
}
