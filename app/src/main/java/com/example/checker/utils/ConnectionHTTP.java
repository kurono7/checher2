package com.example.checker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionHTTP {

    private int statusResponse;
    private String service;

    private ConnetionCallback listener;

    //SERVER
    private final static String SERVER = "http://172.19.15.51:8000";
    //private final static String SERVER = "http://checkerapp.westus2.cloudapp.azure.com:8080";
    private final static int WAIT = 30000;

    // URL API'S
    private final static String AUTENTIFICATION = "/auth/autenticar/mobile";
    public final static String GETTASKS = "/api/v1/tareas/";
    private final static String SIGNOUT = "/api/v1/usuarios/cerrar-sesion/";
    public final static String GETPROYECTS = "/api/v1/general/autenticacion/mobile/";
    private final static String UPDATETASKSTATE = "/api/v1/tareasProyecto/procesar-tarea/";
    private final static String ATTACH_TASK = "/api/v1/tareasProyecto/adjuntar-entregable/";


    public ConnectionHTTP(ConnetionCallback listener) {
        this.listener = listener;
    }

    public void setAttachTask(String IdProyecto, String IdTerritorio, String IdTarea, String token, String image) {
        JSONObject post = new JSONObject();
        try {
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjUxM2U5ZTUwLWVjMTYtNDc1Ny05YWE4LTM5MjM5Yzg2MTEzYyIsImlwIjoiOjoxIiwiaWRVc3VhcmlvIjoiMDJkYjZlZWUtMTdkNC00ZDk0LTgxNmQtODgwYzljZjQwYTRkIiwidXN1YXJpbyI6IkNPT1JCT0dPVEEyIiwibm9tYnJlQ29tcGxldG8iOiJDb29yZGluYWRvciBkZSBTb3BvcnRlIEFNRVJJQ0FTIiwiaWRQZXJmaWwiOiIwNCIsImVtYWlsIjoiTE9SRU5BLkdBVklSSUFAQ0FSVkFKQUwuQ09NIiwiY29kaWdvQ2FyZ28iOiIzIiwiaWF0IjoxNTY0NDk0ODMwLCJleHAiOjE1NjQ1ODEyMzB9.-N8rLm-fCvY5qB0pS6JlfUvGdRjH5bU2ETrrNUTzr20";
            IdProyecto = "78065db9-89c9-45f2-a0b4-c38f5705b037";
            IdTerritorio = "57301f26-cce5-4c00-a099-b97252e102b6";
            IdTarea = "00fb628b-9d87-40fe-9d4a-2781e7d92f48";

            post.put("idProyecto", IdProyecto);
            post.put("idTerritorio", IdTerritorio);
            post.put("idTarea", IdTarea);
            post.put("image", image);
            new SendDeviceDetailsPOST().execute(ATTACH_TASK, post.toString(), token, IdProyecto);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateTaskState(String IdProyecto, String IdTerritorio, String IdTarea, String token) {
        JSONObject post = new JSONObject();
        try {
            post.put("idProyecto", IdProyecto);
            post.put("idTerritorio", IdTerritorio);
            post.put("idTarea", IdTarea);
            new SendDeviceDetailsPOST().execute(UPDATETASKSTATE, post.toString(), token, IdProyecto);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendAutentification(String idUsuario, String nombreUsuario, String claveUsuario, String captcha, String pdata) {
        JSONObject post = new JSONObject();
        try {
            post.put("idUsuario", idUsuario);
            post.put("nombreUsuario", nombreUsuario);
            post.put("claveUsuario", claveUsuario);
            post.put("captcha", captcha);
            post.put("pdata", pdata);
            new SendDeviceDetailsPOST().execute(AUTENTIFICATION, post.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getTasks(String projectID, String territorieID, String responsable, String token) {
        new SendDeviceDetailsGET().execute(GETTASKS, projectID, territorieID, responsable, token);
    }

    public void getproyects(String IdPerfil, String idUsuario, String token) {
        new SendDeviceDetailsGET().execute(GETPROYECTS, idUsuario, IdPerfil, token);
    }

    public void logout(String IdUsuario, String token) {
        new SendDeviceDetailsGET().execute(SIGNOUT, IdUsuario, token);
    }

    public interface ConnetionCallback {
        void onResultReceived(String result, String service);
    }

    @SuppressLint("StaticFieldLeak")
    private class SendDeviceDetailsPOST extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            service = params[0];
            StringBuilder data = new StringBuilder();
            HttpURLConnection httpURLConnection = null;
            try {
                if(params[0].equals(ATTACH_TASK)){
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[3]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[2]);
                }else
                if (params[0].equals(UPDATETASKSTATE)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[3]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[2]);
                }else{
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0]).openConnection();
                }
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(WAIT);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                statusResponse = httpURLConnection.getResponseCode();

                InputStreamReader inputStreamReader;
                if (statusResponse >= 300) {
                    inputStreamReader = new InputStreamReader(httpURLConnection.getErrorStream());
                } else {
                    inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                }

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data.append(current);
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusResponse = 400;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(listener!=null){
                listener.onResultReceived(result, service);
            }
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SendDeviceDetailsGET extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            service = params[0];
            StringBuilder data = new StringBuilder();
            HttpURLConnection httpURLConnection = null;
            try {
                if (params[0].equals(SIGNOUT)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[1]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[2]);
                } else if (params[0].equals(GETPROYECTS)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[2] + "/" + params[1]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[3]);
                } else {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[1] +"/territorio/"+ params[2]+ "?responsable=" + params[3]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[4]);
                }
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(WAIT);

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data.append(current);
                }
                statusResponse = httpURLConnection.getResponseCode();

            } catch (Exception e) {
                statusResponse = 400;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(listener!=null){
                listener.onResultReceived(result, service);
            }
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
