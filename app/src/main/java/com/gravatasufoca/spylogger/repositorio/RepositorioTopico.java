package com.gravatasufoca.spylogger.repositorio;

import com.gravatasufoca.spylogger.model.Topico;

public interface RepositorioTopico extends Repositorio<Topico> {
	Topico findByName(String nome);
	void reativar();
	void limpar();
}
