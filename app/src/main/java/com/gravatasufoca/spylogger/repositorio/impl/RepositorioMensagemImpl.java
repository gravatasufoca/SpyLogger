package com.gravatasufoca.spylogger.repositorio.impl;

import android.content.Context;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioGenerico;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
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
			database.executeRawNoArgs("update mensagem set enviada=0 where topico_id in(select id from topico where tipoMensagem !=2 )");
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
