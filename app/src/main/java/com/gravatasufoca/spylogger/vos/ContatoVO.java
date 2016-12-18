package com.gravatasufoca.spylogger.vos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContatoVO {

	private String nome;
	private String numero;
	private String foto;
	private String fotoBase;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ContatoVO contatoVO = (ContatoVO) o;

		return numero != null ? numero.equals(contatoVO.numero) : contatoVO.numero == null;

	}

	@Override
	public int hashCode() {
		return numero != null ? numero.hashCode() : 0;
	}
}
