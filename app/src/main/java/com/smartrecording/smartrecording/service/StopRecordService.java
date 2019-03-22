package com.smartrecording.smartrecording.service;

import android.content.Context;
import android.os.AsyncTask;

import com.smartrecording.smartrecording.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class StopRecordService extends AsyncTask<String, String, String> {

    private String iURL;

    public StopRecordService(Context context){
        iURL = "http://" + context.getResources().getString(R.string.hostname) + "/api/record/stop";
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(iURL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode=connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {
                    sb.append(line+"\n");
                }
                in.close();
                return sb.toString();
            }
            else {
                return new String("Try Again!");
            }

        } catch (Exception e) {
            return new String("Try Again!");
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String value) {
    }
}
