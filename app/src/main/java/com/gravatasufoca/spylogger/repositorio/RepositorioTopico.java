package com.gravatasufoca.spylogger.repositorio;

import com.gravatasufoca.spylogger.model.Topico;

public interface RepositorioTopico extends Repositorio<Topico> {
	Topico porNome(String nome);
	Topico porReferencia(String referencia);
	void reativar();
	void limpar();
}
