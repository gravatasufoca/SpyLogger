package com.gravatasufoca.spylogger.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;


@DatabaseTable
@Getter
@Setter
public class Gravacao extends EntidadeAbstrata {
	private static final long serialVersionUID = 2717684549689197001L;


	@DatabaseField(generatedId = true)
	private Integer id;

	@DatabaseField(canBeNull=false,dataType=DataType.DATE)
	private Date data=new Date();

	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean enviado;

	@DatabaseField(canBeNull=false,dataType=DataType.BYTE_ARRAY)
	private byte[] audio;

	@DatabaseField(canBeNull=false,dataType=DataType.STRING)
	private String numero;

	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean remetente;

	@DatabaseField
	private long duracao;

	@DatabaseField
	private String nome;

	@DatabaseField(foreign=true, foreignColumnName="id",columnName="topico_id")
	private Topico topico;

	public Serializable getId() {
		return id;
	}

}
