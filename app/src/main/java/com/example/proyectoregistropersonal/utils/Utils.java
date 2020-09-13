package com.example.proyectoregistropersonal.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.example.proyectoregistropersonal.database.DatabaseHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class Utils {

    public static void BD_backup() throws IOException {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());

        final String inFileName = "/data/data/com.example.proyectoregistropersonal/databases/"+ DatabaseHelper.DB_NAME;
        File dbFile = new File(inFileName);
        FileInputStream fis = null;

        fis = new FileInputStream(dbFile);

        String directorio = "/storage/emulated/0/Download/";
        File d = new File(directorio);
        if (!d.exists()) {
            d.mkdir();
        }
        String outFileName = directorio + "/"+DatabaseHelper.DB_NAME + "_"+timeStamp;

        OutputStream output = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        fis.close();

    }

    public static void GuardarFotoCarpeta(Bitmap finalBitmap, String folderName, Context c) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Pictures/RegistroPersonal");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        File myDir2 = new File(root + "/Pictures/RegistroPersonal/" + folderName);
        if (!myDir2.exists()) {
            myDir2.mkdirs();
        }

        File dir = new File(root + "/Pictures/RegistroPersonal/" + folderName); // "/mnt/sdcard/yourfolder"
        long totalNumFiles = dir.listFiles().length;

        String fname = "Image" + totalNumFiles + ".jpg";
        File file = new File(dir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            long imgtotal = totalNumFiles + 1;
            //Toast.makeText(c, "Foto " + imgtotal + " guardada correctamente!", Toast.LENGTH_LONG).show();
            MyToast.showShort(c, "Foto " + imgtotal + " guardada correctamente!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void EliminarFotoCarpeta(Context c) throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        File f = new File(root + "/Pictures/RegistroPersonal");
        FileUtils.deleteDirectory(f);
        MyToast.showShort(c, "Fotos eliminadas correctamente");
    }

    public static void ConexionSSL() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
