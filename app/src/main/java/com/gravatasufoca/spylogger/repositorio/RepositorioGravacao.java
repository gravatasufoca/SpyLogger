package com.gravatasufoca.spylogger.repositorio;

import com.gravatasufoca.spylogger.model.Ligacao;

import java.util.List;


public interface RepositorioGravacao extends Repositorio<Ligacao> {

	List<Ligacao> listarNaoEnviados();

	void reativar();
}
