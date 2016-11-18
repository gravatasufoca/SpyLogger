package com.gravatasufoca.spylogger.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Alarm extends BroadcastReceiver{

	private PendingIntent pi;

	@Override
	 public void onReceive(Context context, Intent intent) {
//			Utils.sendMail(context);
	 }


	 public void SetAlarm(Context context, int minutos)
	    {
	        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        Intent intent = new Intent(context, Alarm.class);
	        pi = PendingIntent.getBroadcast(context, 0, intent, 0);
	        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minutos , pi);
	    }

	    public void CancelAlarm(Context context)
	    {
	        Intent intent = new Intent(context, Alarm.class);
	       // PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	        alarmManager.cancel(pi);
	    }

	    public void setOnetimeTimer(Context context){
	     AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        Intent intent = new Intent(context, Alarm.class);
	        pi = PendingIntent.getBroadcast(context, 0, intent, 0);
	        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
	    }
	}
