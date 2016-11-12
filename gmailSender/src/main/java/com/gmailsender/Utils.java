package com.gmailsender;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.preference.PreferenceManager;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;



public class Utils {


	public static boolean isDebugglabe(Context context) {
		return (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
	}



	public static Account getUserAccount(Context ctx){
		AccountManager am = AccountManager.get(ctx);

		Account[] accounts = am.getAccountsByType("com.google");
		Account me = accounts[0];

		return me;
	}


	public static boolean isConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		State mobile = NetworkInfo.State.DISCONNECTED;
		if (mobileInfo != null) {
			mobile = mobileInfo.getState();
		}
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		State wifi = NetworkInfo.State.DISCONNECTED;
		if (wifiInfo != null) {
			wifi = wifiInfo.getState();
		}
		boolean dataOnWifiOnly = (Boolean) PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean(
						"data_wifi_only", true);
		if ((!dataOnWifiOnly && (mobile.equals(NetworkInfo.State.CONNECTED) || wifi
				.equals(NetworkInfo.State.CONNECTED)))
				|| (dataOnWifiOnly && wifi.equals(NetworkInfo.State.CONNECTED))) {
			return true;
		} else {
			return false;
		}
	}

	public static byte[] getBytes(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis, 128);
		ByteArrayBuffer baf = new ByteArrayBuffer(128);

		// get the bytes one by one
		int current = 0;
		while ((current = bis.read()) != -1) {
			baf.append((byte) current);
		}
		bis.close();

		return baf.buffer();

	}

}
