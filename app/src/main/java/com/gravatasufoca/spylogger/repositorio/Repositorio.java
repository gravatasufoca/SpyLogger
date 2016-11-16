package com.gravatasufoca.spylogger.repositorio;


import android.content.Context;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;


/**
 * Interface que define um repositrio de entidades.
 *
 * @author bruno.canto
 *
 * @param <E>
 *            Tipo da entidade.
 */
public interface Repositorio<E> {

	/**
	 * Obtm do repositrio a entidade com o id dado.
	 *
	 * @param id
	 *            id para a busca.
	 * @return entidade encontrada.
	 */
	E obterPorId(Serializable id);

	/**
	 * Lista todas as entidades armazenadas no repositrio.
	 *
	 * @return entidades armazenadas.
	 */
	List<E> listar();

	/**
	 * Insere uma entidade no repositrio.
	 *
	 * @param entidade
	 *            entidade para inserir.
	 * @throws SQLException
	 */
	void inserir(E entidade) throws SQLException;

	/**
	 * Atualiza uma entidade j armazenada.
	 *
	 * @param entidade
	 *            entidade para atualizar.
	 * @throws SQLException
	 */
	E atualizar(E entidade) throws SQLException;

	/**
	 * Remove uma entidade do repositrio
	 *
	 * @param entidade
	 *            para remover.
	 * @throws SQLException
	 */
	void excluir(E entidade) throws SQLException;

	/**
	 * Remove a entidade cujo id  o informado.
	 *
	 * @param id
	 *            id da entidade a remover.
	 * @throws SQLException
	 */
	void excluir(Serializable id) throws SQLException;

	void excluir(List<E> entidades) throws SQLException;

	/**
	 * Insere a entidade caso seu id seja nulo ou a atualiza caso contrrio.
	 *
	 * @param entidade
	 *            entidade para a operao.
	 * @throws SQLException
	 */
	void inserirOuAtualizar(E entidade) throws SQLException;
	Context getContext();
}
