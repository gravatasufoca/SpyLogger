package com.gravatasufoca.spylogger.dao;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	public static void main(String[] args) throws Exception {
		File conffile = new File("app/src/main/res/raw/ormlite_config.txt");
		File dir = new File("app/src/main/java/com/gravatasufoca/spylogger/model/");
		writeConfigFile(conffile,dir);
	}
}