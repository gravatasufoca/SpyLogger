package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.service.notification.StatusBarNotification;

/**
 * Created by bruno on 04/06/17.
 */

public class MensagemNotificationFactory {

    private static final String WHATS="com.whatsapp";
    private static final String MESSENGER="com.facebook.orca";

    public static MensagemNotificacao build(Context context, StatusBarNotification sbn){
        switch (sbn.getPackageName()){
            case WHATS:
                return new WhatsNotificationAdd(context,sbn);
//            case MESSENGER:
//                return new MessengerNotificationAdd(context,sbn);
            default:
                return null;
        }
    }

}