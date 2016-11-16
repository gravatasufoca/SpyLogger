package com.gravatasufoca.spylogger.repositorio.impl;

import android.content.Context;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class RepositorioMensagemImpl extends RepositorioGenerico<Mensagem> implements RepositorioMensagem {

	private Context context;
	public RepositorioMensagemImpl(Context context) throws SQLException {
		super(context);
	}

	public Context getContext() {
		return context;
	}


	@Override
	public List<Mensagem> listarNaoEnviados() {
		try {
			return database.queryBuilder().where().eq("enviado", false).query();
		} catch (SQLException e) {
			return Collections.emptyList();
		}
	}



}
