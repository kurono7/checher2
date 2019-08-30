package com.innovacion.checker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

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
    //private final static String SERVER = "http://192.168.43.218:8000";
    //private final static String SERVER = "http://checkerapp.westus2.cloudapp.azure.com:8080";
    private final static String SERVER = "https://api.checkerapp.co";
    private final static int WAIT = 20000;

    // URL API'S
    private final static String AUTHENTICATION = "/auth/autenticar/mobile";
    public final static String GETTASKS = "/api/v1/tareas/";
    public final static String SIGNOUT = "/api/v1/usuarios/cerrar-sesion/";
    public final static String GETPROJECTS = "/api/v1/general/autenticacion/mobile/";
    private final static String UPDATETASKSTATE = "/api/v1/tareasProyecto/procesar-tarea/";
    public final static String ATTACH_TASK = "/api/v1/tareasProyecto/adjuntar-entregable/";
    public final static String GETMESSAGE = "/api/v1/tareasProyecto/mensajes/";

    public ConnectionHTTP(ConnetionCallback listener) {
        this.listener = listener;
    }

    public void setAttachTask(String IdProyecto, String IdTerritorio, String IdTarea, String token, String image, String comment) {
        JSONObject post = new JSONObject();
        try {
            post.put("idProyecto", IdProyecto);
            post.put("idTerritorio", IdTerritorio);
            post.put("idTarea", IdTarea);
            post.put("image", image);
            post.put("comment", comment);
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

    public void sendAuthentication(String idUsuario, String nombreUsuario, String claveUsuario, String captcha, String pdata) {
        JSONObject post = new JSONObject();
        try {
            post.put("idUsuario", idUsuario);
            post.put("nombreUsuario", nombreUsuario);
            post.put("claveUsuario", claveUsuario);
            post.put("captcha", captcha);
            post.put("pdata", pdata);
            new SendDeviceDetailsPOST().execute(AUTHENTICATION, post.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getTasks(String projectID, String territorieID, String responsable, String token) {
        new SendDeviceDetailsGET().execute(GETTASKS, projectID, territorieID, responsable, token);
    }

    public void getProjects(String IdPerfil, String idUsuario, String token) {
        new SendDeviceDetailsGET().execute(GETPROJECTS, idUsuario, IdPerfil, token);
    }

    public void logout(String IdUsuario, String token) {
        new SendDeviceDetailsGET().execute(SIGNOUT, IdUsuario, token);
    }

    public void getMessage(String idProject, String idTerritorie, String idTarea, String token) {
        JSONObject post = new JSONObject();
        try {
            post.put("idProyecto", idProject);
            post.put("idTerritorio", idTerritorie);
            post.put("idTarea", idTarea);
            new SendDeviceDetailsPOST().execute(GETMESSAGE, post.toString(), idProject, token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                if (params[0].equals(ATTACH_TASK)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[3]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[2]);
                } else if (params[0].equals(UPDATETASKSTATE)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[3]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[2]);
                } else if (params[0].equals(GETMESSAGE)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[2]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[3]);
                } else {
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
            if (listener != null) {
                listener.onResultReceived(result, service);
            } // this is expecting a response code to be sent from your server upon receiving the POST data
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
                } else if (params[0].equals(GETPROJECTS)) {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[2] + "/" + params[1]).openConnection();
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + params[3]);
                } else {
                    httpURLConnection = (HttpURLConnection) new URL(SERVER + params[0] + params[1] + "/territorio/" + params[2] + "?responsable=" + params[3]).openConnection();
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
            if (listener != null) {
                listener.onResultReceived(result, service);
            } // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
