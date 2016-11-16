package com.gravatasufoca.spylogger.repositorio;

import com.gravatasufoca.spylogger.model.Gravacao;

import java.util.List;


public interface RepositorioGravacao extends Repositorio<Gravacao> {

	List<Gravacao> listarNaoEnviados();

}
