package com.gravatasufoca.spylogger.model.messenger;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="contacts")
public class Contact extends EntidadeAbstrata{

	private static final long serialVersionUID = 723873977185254805L;

	public Contact() {
	}

	@DatabaseField(id=true,generatedId=false)
	private int internal_id;

	@DatabaseField()
	private String fbid;

	@DatabaseField(columnName="small_picture_url",canBeNull=false)
	private String smallPictureUrl;

	@DatabaseField(columnName="small_picture_size",canBeNull=false)
	private int smallPictureSize;


	@Override
	public Serializable getId() {
		return internal_id;
	}

	public String getFbid() {
		return fbid;
	}

	public void setFbid(String fbid) {
		this.fbid = fbid;
	}

	public String getSmallPictureUrl() {
		return smallPictureUrl;
	}


	public void setSmallPictureUrl(String smallPictureUrl) {
		this.smallPictureUrl = smallPictureUrl;
	}


	public int getSmallPictureSize() {
		return smallPictureSize;
	}


	public void setSmallPictureSize(int smallPictureSize) {
		this.smallPictureSize = smallPictureSize;
	}



}
