package com.gravatasufoca.spylogger.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.helpers.TaskComplete;

import java.util.ArrayList;
import java.util.List;

public class Alarm extends BroadcastReceiver {

    private TaskComplete callBack;
    private List<PendingIntent> intents=new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(callBack!=null){
            callBack.onFinish(null);
        }
    }

    public PendingIntent setRepeatingAlarm(Context context,PendingIntent pi, int minutos, TaskComplete callback) {
        this.callBack=callback;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minutos, pi);
        intents.add(pi);
        return pi;
    }

    public PendingIntent setRepeatingAlarm(Context context, int minutos, TaskComplete callback) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return setRepeatingAlarm(context,pi,minutos,callback);
    }

    public void cancelAlarm(Context context,PendingIntent pi) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
    }

    public void cancelAll(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for(PendingIntent pi: intents){
            alarmManager.cancel(pi);
        }
    }

}
