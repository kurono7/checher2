package com.example.checker.utils;

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
    public final static String SERVER = "http://checkerapp.westus2.cloudapp.azure.com:8080";
    public final static int WAIT = 30000;

    // URL API'S
    public final static String AUTENTIFICATION = "/auth/autenticar/mobile";
    public final static String GETTASKS = "/api/v1/tareas/busqueda/tareas-usuario/";
    public final static String SIGNOUT = "/api/v1/usuarios/cerrar-sesion/";
    public final static String GETPROYECTS = "/api/v1/general/autenticacion/mobile/";
    public final static String UPDATETASKSTATE = "/api/v1/tareasProyecto/procesar-tarea/";


    public ConnectionHTTP(ConnetionCallback listener) {
        this.listener = listener;
    }

    public int getStatusResponse() {
        return statusResponse;
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

    public void getTasks(String projectID, String responsable, String token) {
        new SendDeviceDetailsGET().execute(GETTASKS, projectID, responsable, token);
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

    private class SendDeviceDetailsPOST extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            service = params[0];
            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
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

                InputStreamReader inputStreamReader = null;
                if (statusResponse >= 300) {
                    inputStreamReader = new InputStreamReader(httpURLConnection.getErrorStream());
                } else {
                    inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                }

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusResponse = 400;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data;
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

    private class SendDeviceDetailsGET extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            service = params[0];
            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                if (params[0].equals(SIGNOUT)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[1]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[2]);
                } else if (params[0].equals(GETPROYECTS)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[2] + "/" + params[1]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[3]);
                } else {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[1] + "?responsable=" + params[2]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[3]);
                }
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(WAIT);

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
                statusResponse = httpURLConnection.getResponseCode();

            } catch (Exception e) {
                statusResponse = 400;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data;
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

    private class SendDeviceDetailsDELETE extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            service = params[0];
            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("DELETE");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(WAIT);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                statusResponse = httpURLConnection.getResponseCode();

                InputStreamReader inputStreamReader = null;
                if (statusResponse >= 300) {
                    inputStreamReader = new InputStreamReader(httpURLConnection.getErrorStream());
                } else {
                    inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                }

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }

            } catch (Exception e) {
                e.printStackTrace();
                statusResponse = 400;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
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
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
