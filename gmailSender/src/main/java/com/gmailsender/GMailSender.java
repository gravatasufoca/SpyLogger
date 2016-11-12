package com.gmailsender;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;
import com.utilidades.gravata.utils.Utilidades;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;



public class GMailSender {
	private Session session;
	private String token,user;
	private Multipart multipart;
	private Context context;
	private SharedPreferences sharedPrefs;
	private boolean auth=false;
	private boolean iniciado=false;

	public String getToken() {
		return token;
	}

	public GMailSender(Context ctx) {
		super();
		context=(Context) ctx;
		sharedPrefs=PreferenceManager.getDefaultSharedPreferences(context);

		multipart = new MimeMultipart();
		initToken();

	}

	public Context getContext() {
		return context;
	}

	public void initToken() {


		Account me = Utilidades.getUserAccount(context);
		user=me.name;
		String oldToken=sharedPrefs.getString("token", null);
		if(oldToken!=null)
			GoogleAuthUtil.invalidateToken(context, oldToken);

		new Thread(new Runnable() {

			@Override
			public void run() {
					try {
						token=Utilidades.getTokenAuth(context);
					} catch (UserRecoverableAuthException e) {
						Log.d("initToken callback",e.getMessage());

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (GoogleAuthException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					iniciado=true;
				Log.d("initToken callback", "token=" + token);
			}
		}).start();

		Log.d("getToken", "token=" + token);
	}

	public SMTPTransport connectToSmtp(String host, int port, String userEmail,
			String oauthToken, boolean debug) throws Exception {

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.sasl.enable", "false");

		session = Session.getInstance(props);
		session.setDebug(debug);

		final URLName unusedUrlName = null;
		SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
		// If the password is non-null, SMTP tries to do AUTH LOGIN.
		final String emptyPassword = null;

		/*
		 * enable if you use this code on an Activity (just for test) or use the
		 * AsyncTask StrictMode.ThreadPolicy policy = new
		 * StrictMode.ThreadPolicy.Builder().permitAll().build();
		 * StrictMode.setThreadPolicy(policy);
		 */

		transport.connect(host, port, userEmail, emptyPassword);

		byte[] response = String.format("user=%s\1auth=Bearer %s\1\1",
				userEmail, oauthToken).getBytes();
		response = BASE64EncoderStream.encode(response);

		transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);

		return transport;
	}

	public void sendMail(String subject, String body, String oauthToken, String recipients) throws Exception {
			SMTPTransport smtpTransport;
			try{
				smtpTransport = connectToSmtp("smtp.gmail.com", 587, user, oauthToken != null ? oauthToken : token, true);
			}catch(Exception e){
				iniciado=false;
				initToken();
				while(!iniciado){}
				smtpTransport = connectToSmtp("smtp.gmail.com", 587, user,token, true);

			}

			MimeMessage message = new MimeMessage(session);

//			DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));

			message.setSender(new InternetAddress(user));
			message.setSubject(subject);
//			message.setDataHandler(handler);
			if (recipients.indexOf(',') > 0)
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(recipients));
			else
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(recipients));


			// setup message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			message.setContent(multipart);
			smtpTransport.sendMessage(message, message.getAllRecipients());

	}

	public void clearAttachments(){
		multipart = new MimeMultipart();
	}

	public void addAttachment(String filename, byte[] file) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		BufferedDataSource bds = new BufferedDataSource(file, "AttName");
		messageBodyPart.setDataHandler(new DataHandler(bds));
		messageBodyPart.setFileName(filename);

		multipart.addBodyPart(messageBodyPart);
	}

	public void addAttachment(String filename,File file) throws Exception {
		MimeBodyPart attachPart = new MimeBodyPart();
		attachPart.attachFile(file);
		attachPart.setFileName(filename);
		multipart.addBodyPart(attachPart);
	}

}