package com.gravatasufoca.spylogger.repositorio;

import com.gravatasufoca.spylogger.model.Mensagem;

import java.util.List;

public interface RepositorioMensagem extends Repositorio<Mensagem> {
	List<Mensagem> listarNaoEnviados();
}
