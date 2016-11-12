package com.gravatasufoca.spylogger.vos;

import java.io.File;

public class ContatoVO {

	private String nome;
	private File foto;
	private String fotoBase;


	public ContatoVO(String nome) {
		this.nome=nome;
	}


	public String getNome() {
		return nome;
	}


	public void setNome(String nome) {
		this.nome = nome;
	}


	public File getFoto() {
		return foto;
	}


	public void setFoto(File foto) {
		this.foto = foto;
	}


	public String getFotoBase() {
		return fotoBase;
	}


	public void setFotoBase(String fotoBase) {
		this.fotoBase = fotoBase;
	}



}
