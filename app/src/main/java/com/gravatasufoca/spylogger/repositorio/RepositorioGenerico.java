package com.gravatasufoca.spylogger.repositorio;

import android.content.Context;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import tools.devnull.trugger.reflection.Reflection;

public class RepositorioGenerico<E> extends DatabaseHelper implements Repositorio<E>{

	protected Dao<E, Integer> database;
	protected Class<E> entityClass;
	protected Context context;

	public RepositorioGenerico(Context context) throws SQLException {
		super(context);
		this.entityClass = Reflection.reflect().genericType("E").in(this);
	    this.database=getDao(this.entityClass);
	    this.context=context;
	}

	public RepositorioGenerico(Context context,boolean fromHelper) throws SQLException {
		super(context);
		this.entityClass = Reflection.reflect().genericType("E").in(context);
	    this.database=getDao(this.entityClass);
	}

	@Override
	public E obterPorId(Serializable id) {
		try {
			return (E) database.queryForId((Integer) id);
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public List<E> listar() {

		try {
			return (List<E>) database.queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return Collections.emptyList();
		}
	}

	protected List<E> listarPorAtributo(String campo, Object valor) {

		try {
			return (List<E>) database.queryForEq(campo, valor);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return Collections.emptyList();
		}
	}

	protected E recuperarPorAtributo(String campo, Object valor) throws SQLException{

		return database.queryForFirst(database.queryBuilder().where().eq(campo, valor).prepare() );
	}

	@Override
	public void inserir(E entidade) throws SQLException {
		database.create(entidade);

	}

	@Override
	public E atualizar(E entidade) throws SQLException {
		database.update(entidade);
		return entidade;
	}

	@Override
	public void excluir(E entidade) throws SQLException {
		database.delete(entidade);

	}

	@Override
	public void excluir(Serializable id) throws SQLException {
		database.deleteById((Integer) id);
	}

	@Override
	public void excluir(List<E> entidades) throws SQLException {
		for(E e:entidades){
			database.delete(e);
		}

	}

	@Override
	public void inserirOuAtualizar(E entidade) throws SQLException {
		database.createOrUpdate(entidade);

	}

	protected Dao<E, Integer> getDatabase() {
		return database;
	}

	@Override
	public Context getContext() {
		return context;
	}

	public void close(){
		super.close();
	}
}
