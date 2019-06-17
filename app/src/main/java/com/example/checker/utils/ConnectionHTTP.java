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

    private String response;
    private int statusResponse;
    private boolean finishProcess;

    //SERVER
    public final static String SERVER = "http://checkerapp.westus2.cloudapp.azure.com:8000";
    public final static int WAIT = 20000;

    // URL API'S
    public final static String AUTENTIFICATION = "/api/user/login";
    public final static String GETTASKS = "/api/v1/tareas/busqueda/tareas-usuario/";

    public ConnectionHTTP() {
        finishProcess = false;
    }

    public String getResponse() {
        return response;
    }

    public int getStatusResponse() {
        return statusResponse;
    }

    public boolean isFinishProcess() {
        return finishProcess;
    }

    public void sendAutentification(String mail, String password) {
        JSONObject post = new JSONObject();
        try {
            post.put("mail", mail);
            post.put("password", password);
            new SendDeviceDetailsPOST().execute(SERVER + AUTENTIFICATION, post.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getTasks(String projectID, String responsable) {
        new SendDeviceDetailsGET().execute(SERVER + GETTASKS, projectID, responsable);
    }

    private class SendDeviceDetailsPOST extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);

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
                response = data;
                finishProcess = true;

            } catch (Exception e) {
                e.printStackTrace();
                response = "Error al conectar";
                statusResponse = 400;
                finishProcess = true;
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
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    private class SendDeviceDetailsGET extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0] + params[1] + "?responsable=" + params[2]).openConnection();
                httpURLConnection.setRequestMethod("GET");
                //httpURLConnection.setRequestProperty("Content-Type", "application/json");
                //httpURLConnection.setRequestProperty("Accept", "application/json");
                //httpURLConnection.setDoOutput(true);

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }

                response = data;
                statusResponse = httpURLConnection.getResponseCode();
                finishProcess = true;

            } catch (Exception e) {
                response = "Error al conectar";
                statusResponse = 400;
                finishProcess = true;
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
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    private class SendDeviceDetailsDELETE extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("DELETE");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);

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
                response = data;
                finishProcess = true;

            } catch (Exception e) {
                e.printStackTrace();
                response = "Error al conectar";
                statusResponse = 400;
                finishProcess = true;
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
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
