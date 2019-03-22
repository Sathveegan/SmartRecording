package com.smartrecording.smartrecording;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smartrecording.smartrecording.service.StartRecordService;
import com.smartrecording.smartrecording.service.StopRecordService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AutoActivity extends Activity {

    final private int Request_permission_code = 1000;
    private Timer timer;
    private String pathSave = "";
    MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getResources().getString(R.string.app_name) + "/.auto";
        File f = new File(folder);
        if(!f.exists())
            f.mkdir();

        timer = new Timer();

        if (checkPermissionFromDevice())
            RequestPermissions();

        Button btn_start = (Button) findViewById(R.id.btnStart);
        Button btn_stop = (Button) findViewById(R.id.btnStop);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AutoActivity.this, "Auto Recording Start", Toast.LENGTH_SHORT).show();
                startRecord();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        stopRecord(pathSave);
                        startRecord();
                    }
                }, 20*1000, 20*1000);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AutoActivity.this, "Auto Recording Stop", Toast.LENGTH_SHORT).show();
                stopRecord(pathSave);
                timer.cancel();
                timer.purge();
                StopRecordService tsk = new StopRecordService(AutoActivity.this);
                tsk.execute();
            }
        });
    }

    private void RequestPermissions() {

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO
        }, Request_permission_code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case Request_permission_code:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granded", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissionFromDevice() {

        int write_external_storage_result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    private void startRecord(){
        if (checkPermissionFromDevice()) {

            pathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getResources().getString(R.string.app_name) + "/.auto/" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".mp3";
            setupMediaRecorder();

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            RequestPermissions();
        }
    }

    private void stopRecord(String pathSave){
        mediaRecorder.stop();
        StartRecordService tsk = new StartRecordService(AutoActivity.this, pathSave);
        tsk.execute();
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }

}
