package com.gravatasufoca.spylogger.model.messenger;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="preferences")
public class Prefs extends EntidadeAbstrata{

	private static final long serialVersionUID = 5129217870660620874L;

	public Prefs() {
	}

	@DatabaseField(id=true,generatedId=false)
	private String key;

	@DatabaseField()
	private int type;

	@DatabaseField()
	private String value;

	@Override
	public Serializable getId() {
		return key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
