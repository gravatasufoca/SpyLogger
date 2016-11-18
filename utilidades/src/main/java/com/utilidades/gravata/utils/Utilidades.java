package com.utilidades.gravata.utils;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.utilidades.gravata.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import com.google.android.gms.auth.GoogleAuthUtil;

public class Utilidades {
	public static String BASE64_PUBLIC_KEY = "";
	public static final byte[] SALT = new byte[]{-92, 98, -32, 29, 45, 64,
			73, 64, 35, -13, -12, -9, -3, 5, -23, 23, -94, 123, -11, 4};
	public static String SKU = "premium";
	private static String PAYLOADER = "";
	public static boolean premium = false;
	public static boolean verificado = false;
	public static String PREF = "ja";

	public static boolean isDebugglabe(Context context) {
		return (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
	}

	public static void verifyLicense(Context ctx) {


	}

	public static String getDeviceId(ContentResolver contentResolver) {
		String deviceId = Secure.getString(contentResolver, Secure.ANDROID_ID);
		return "_#$A12abdek%" + deviceId;
	}

	private static boolean isComprado(final Context ctx) {
		if (!isDebugglabe(ctx)) {
			boolean t = ctx.getSharedPreferences(PREF, 0).getBoolean("bb", false);
			premium = t;
			verificado = true;
			return t;
		} else {
			premium = true;
			verificado = true;
			ctx.getSharedPreferences(PREF, 0).edit().putBoolean("bb", false).commit();
			return true;
		}
	}

	public synchronized static void verifyPremium(final Context ctx) {
		premium = true;
		verificado=true;
	}

	public static void showPremiumMessage(Context ctx) {
		Toast.makeText(ctx, R.string.premium_message, Toast.LENGTH_LONG).show();
	}

	public static void showPremiumMessage(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
	}

	public static boolean isPremium(Context context) {

		if (isDebugglabe(context))
			return true;

		if (isComprado(context)) {
			return true;
		}

		if (isConnected(context)) {
			if (!verificado)
				verifyPremium(context);
		}
		return premium;

	}

	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}
		return false;
	}

	public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static String getTokenAuth(Context context) throws UserRecoverableAuthException, IOException, GoogleAuthException {
		return GoogleAuthUtil.getToken(context, getUserAccount(context).name, "oauth2:https://mail.google.com/");
	}

	public static Account getUserAccount(Context ctx) {
		AccountManager am = AccountManager.get(ctx);

		Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

		return accounts[0];
	}

	public static void askPermissions(Activity context, String... permissions) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			List<String> necessarias=new ArrayList<>();
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					necessarias.add(permission);
				}
			}
			ActivityCompat.requestPermissions(context, (String[]) necessarias.toArray(),1);
		}
	}
}
