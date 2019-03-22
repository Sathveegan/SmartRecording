package com.smartrecording.smartrecording.service;

import android.content.Context;
import android.os.AsyncTask;

import com.smartrecording.smartrecording.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class StartRecordService extends AsyncTask<String, String, String> {

    private String iURL;
    private String dataSource;
    private String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
    private int bytesRead, bytesAvailable, bufferSize;
    private byte[] buffer;
    private int maxBufferSize = 1 * 1024 * 1024;

    public StartRecordService(Context context, String dataSource) {
        this.dataSource = dataSource;
        iURL = "http://" + context.getResources().getString(R.string.hostname) + "/api/record/start";
    }

    @Override
    protected String doInBackground(String... s) {
        try {
            URL url = new URL(iURL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            File sourceFile = new File(dataSource);
            FileInputStream fileInputStream = new FileInputStream(sourceFile);

            out.writeBytes("--" + boundary + "\r\nContent-Disposition: form-data; name=\"audio\";filename=\"" + sourceFile.getName() + "\"\r\nContent-Type: audio/3gp" + "\r\n\r\n");

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                out.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            out.writeBytes("\r\n");


            out.writeBytes("--" + boundary + "--");
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line = "";

                while ((line = in.readLine()) != null) {
                    sb.append(line + "\n");
                }
                in.close();
                return sb.toString();
            } else {
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
        new File(dataSource).delete();
    }
}
