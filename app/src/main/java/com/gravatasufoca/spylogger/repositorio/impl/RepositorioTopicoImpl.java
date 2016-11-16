package com.gravatasufoca.spylogger.repositorio.impl;

import android.content.Context;

import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;

import java.sql.SQLException;

public class RepositorioTopicoImpl extends RepositorioGenerico<Topico> implements RepositorioTopico {

	private Context context;
	public RepositorioTopicoImpl(Context context) throws SQLException {
		super(context);
	}

	public Context getContext() {
		return context;
	}

	@Override
	public Topico findByName(String nome) {
		try {
			return database.queryForFirst(database.queryBuilder().where().eq("nome",nome).prepare());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
