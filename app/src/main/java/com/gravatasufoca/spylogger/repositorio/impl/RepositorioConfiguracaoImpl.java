package com.gravatasufoca.spylogger.repositorio.impl;

import android.content.Context;

import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;

import java.sql.SQLException;

public class RepositorioConfiguracaoImpl extends RepositorioGenerico<Configuracao> implements RepositorioConfiguracao {

	private Context context;
	public RepositorioConfiguracaoImpl(Context context) throws SQLException {
		super(context);
	}

	public Context getContext() {
		return context;
	}

	@Override
	public Configuracao getConfiguracao() {
		try {
			return database.queryForAll().get(0);
		} catch (SQLException e) {
			return null;
		}
	}
}
