package com.gravatasufoca.spylogger.dao;

import android.support.v7.app.ActionBarActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;


public class OrmLiteActionBarActitivy extends ActionBarActivity {

    private static DatabaseHelper databaseHelper = null;

    protected DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper =
                OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}