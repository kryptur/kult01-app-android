package de.lbader.apps.kult01;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestTask extends AsyncTask<String, String, String> {
    public interface RequestCallback {
        public void callback(JSONObject res);
    }

    private RequestCallback callback;
    private String apiFunction;

    private String baseURL = "http://kult01.de/api_";
    private String urlExtension = ".aspx";

    private int responseCode = -1;

    public RequestTask(String apiFunction, RequestCallback callback) {
        this.apiFunction = apiFunction;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... useless) {
        try {
            URL url = new URL(baseURL + apiFunction + urlExtension);
            Log.e("API", "URL: " + url.toString());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                return response;
            } else {
                return null;
            }

        } catch (MalformedURLException ex) {
            Log.e("API", "ERROR A: " + ex.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("API", "ERROR B: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result == null) {
            try {
                JSONObject obj = new JSONObject("");
                callback.callback(obj);
            } catch (Exception ex) {
                callback.callback(null);
            }
        } else {
            try {
                JSONObject obj = new JSONObject(result);
                callback.callback(obj);
            } catch (JSONException ex) {
                Log.e("API", "Could not parse JSON result: " + ex.getMessage());
                Log.e("API", "Invalid result: " + result);
                ex.printStackTrace();
                callback.callback(null);
            }
        }
    }
}
