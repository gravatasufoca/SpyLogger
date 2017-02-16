package com.gravatasufoca.spylogger.repositorio.impl;

import android.content.Context;

import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.j256.ormlite.table.TableUtils;

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

	@Override
	public void reativar() {
		try {
			database.executeRawNoArgs("update topico set enviado=0 where tipoMensagem!=2 ");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void limpar() {
		try {
			TableUtils.dropTable(getConnectionSource(),Topico.class,true);
			TableUtils.createTable(getConnectionSource(),Topico.class);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
